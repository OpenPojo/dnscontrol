/*
 * Copyright (c) 2018-2018 Osman Shoukry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openpojo.dns;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.openpojo.dns.cache.JdkAddressCache;
import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.resolve.RoutingResolver;
import com.openpojo.dns.routing.RoutingTable;
import com.openpojo.dns.service.initialize.Initializer;
import com.openpojo.dns.testing.FakeDnsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Type;
import org.xbill.DNS.hosts.HostsFileParser;

import static com.openpojo.dns.cache.JdkAddressCache.SUN_NET_INETADDR_TTL;
import static com.openpojo.dns.testing.CapturedLogs.capturingLogsOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

/**
 * DnsControl as a plugin on dnsjava: install() wires the routing table, hosts file and caches into Lookup so that any
 * dnsjava user, and later the JVM resolver provider, gets DNSControl's behaviour.
 */
class DnsControlInstallTest {
  private static final String JDK_NET_HOSTS_FILE = "jdk.net.hosts.file";
  private FakeDnsServer domainServer;

  @BeforeEach
  void configureARouteToAFakeServer() throws Exception {
    Lookup.refreshDefault();
    // The JDK may already have loaded the provider (and installed DNSControl) earlier in this JVM; start clean.
    DnsControl.recreateInstance();
    domainServer = FakeDnsServer.start();
    Path config = Files.createTempFile("dnscontrol", ".conf");
    Files.writeString(config, ".a.test=" + domainServer.hostAndPort() + "\n");
    System.setProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE, config.toString());
  }

  @AfterEach
  void restoreDnsjavaDefaults() {
    System.clearProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE);
    System.clearProperty(JDK_NET_HOSTS_FILE);
    System.clearProperty(SUN_NET_INETADDR_TTL);
    System.clearProperty(DnsControl.DNSJAVA_HOSTS_FILE_CHECK_INTERVAL);
    System.clearProperty(DnsControl.ENABLED);
    System.clearProperty(Initializer.DNSCONTROL_SEARCH_DOMAIN);
    Lookup.refreshDefault();
    DnsControl.recreateInstance();
    domainServer.close();
  }

  @Test
  void shouldRouteLookupsThroughTheConfiguredRoutingTable() throws Exception {
    DnsControl.install();

    Record question = Record.newRecord(Name.fromString("host.a.test."), Type.A, DClass.IN);
    new Lookup(question.getName(), Type.A).run();

    assertThat(domainServer.queries(), contains(question));
  }

  @Test
  void shouldAnswerFromTheHostsFileBeforeAskingAnyServer() throws Exception {
    hostsFileWith("10.0.0.5 server50.a.test");
    DnsControl.install();

    Record[] answers = new Lookup("server50.a.test.", Type.A).run();

    assertThat(((ARecord) answers[0]).getAddress(), is(InetAddress.getByName("10.0.0.5")));
    assertThat(domainServer.queries(), empty());
  }

  @Test
  void shouldPickUpChangesToTheHostsFileWithoutARestart() throws Exception {
    Path hosts = hostsFileWith("10.0.0.5 server50.a.test");
    System.setProperty(DnsControl.DNSJAVA_HOSTS_FILE_CHECK_INTERVAL, "0");
    DnsControl.install();
    new Lookup("server50.a.test.", Type.A).run();

    Files.writeString(hosts, "10.0.0.6 server50.a.test\n");
    Files.setLastModifiedTime(hosts, FileTime.from(Instant.now().plusSeconds(5)));

    assertThat(addressWithinTwoSeconds("server50.a.test.", InetAddress.getByName("10.0.0.6")), is(InetAddress.getByName("10.0.0.6")));
  }

  @Test
  void shouldCheckTheHostsFileForChangesEverySecondUnlessToldOtherwise() throws Exception {
    hostsFileWith("10.0.0.5 server50.a.test");

    DnsControl.install();

    assertThat(System.getProperty(DnsControl.DNSJAVA_HOSTS_FILE_CHECK_INTERVAL), is("1000"));
  }

  @Test
  void shouldLeaveAnExplicitHostsFileCheckIntervalAlone() throws Exception {
    hostsFileWith("10.0.0.5 server50.a.test");
    System.setProperty(DnsControl.DNSJAVA_HOSTS_FILE_CHECK_INTERVAL, "12345");

    DnsControl.install();

    assertThat(System.getProperty(DnsControl.DNSJAVA_HOSTS_FILE_CHECK_INTERVAL), is("12345"));
  }

  @Test
  void shouldWarnAboutTheJdksOwnThirtySecondCacheWhenNothingConfiguresIt() {
    List<LogRecord> records = capturingLogsOf(JdkAddressCache.class, DnsControl::install);

    assertThat(records, hasSize(1));
    assertThat(records.get(0).getLevel(), is(Level.WARNING));
    assertThat(records.get(0).getMessage(), allOf(containsString("30 s"), containsString("networkaddress.cache.ttl"),
        containsString("sun.net.inetaddr.ttl")));
  }

  @Test
  void shouldNotWarnWhenTheJdkCacheIsTurnedOffWithTheSystemProperty() {
    System.setProperty(SUN_NET_INETADDR_TTL, "0");

    assertThat(capturingLogsOf(JdkAddressCache.class, DnsControl::install), empty());
  }

  @Test
  void shouldInstallOnlyOnce() {
    DnsControl first = DnsControl.install();
    RoutingTable installedTable = ((RoutingResolver) Lookup.getDefaultResolver()).getRoutingTable();

    DnsControl second = DnsControl.install();

    assertThat(first, sameInstance(DnsControl.getInstance()));
    assertThat(second, sameInstance(first));
    assertThat(((RoutingResolver) Lookup.getDefaultResolver()).getRoutingTable(), sameInstance(installedTable));
  }

  @Test
  void uninstallShouldRestoreWhatDnsjavaHadBefore() throws Exception {
    Resolver resolverBefore = Lookup.getDefaultResolver();
    HostsFileParser parserBefore = Lookup.getDefaultHostsFileParser();
    hostsFileWith("10.0.0.5 server50.a.test");
    DnsControl.install();
    assertThat(Lookup.getDefaultResolver(), not(sameInstance(resolverBefore)));
    assertThat(Lookup.getDefaultHostsFileParser(), not(sameInstance(parserBefore)));

    DnsControl.uninstall();

    assertThat(Lookup.getDefaultResolver(), sameInstance(resolverBefore));
    assertThat(Lookup.getDefaultHostsFileParser(), sameInstance(parserBefore));
  }

  @Test
  void uninstallShouldStopWatchingTheHostsFile() throws Exception {
    HostsFileParser parserBefore = Lookup.getDefaultHostsFileParser();
    Path hosts = hostsFileWith("10.0.0.5 server50.a.test");
    System.setProperty(DnsControl.DNSJAVA_HOSTS_FILE_CHECK_INTERVAL, "0");
    DnsControl.install();

    DnsControl.uninstall();
    Files.writeString(hosts, "10.0.0.6 server50.a.test\n");
    Files.setLastModifiedTime(hosts, FileTime.from(Instant.now().plusSeconds(5)));
    Thread.sleep(100);

    assertThat(Lookup.getDefaultHostsFileParser(), sameInstance(parserBefore));
  }

  @Test
  void shouldInstallNothingWhenDisabled() throws Exception {
    Resolver resolverBefore = Lookup.getDefaultResolver();
    HostsFileParser parserBefore = Lookup.getDefaultHostsFileParser();
    hostsFileWith("10.0.0.5 server50.a.test");
    System.setProperty(DnsControl.ENABLED, "false");

    DnsControl.install();

    assertThat(Lookup.getDefaultResolver(), sameInstance(resolverBefore));
    assertThat(Lookup.getDefaultHostsFileParser(), sameInstance(parserBefore));
  }

  @Test
  void shouldSearchRelativeNamesInTheConfiguredDomain() throws Exception {
    System.setProperty(Initializer.DNSCONTROL_SEARCH_DOMAIN, "a.test.");
    DnsControl.install();

    new Lookup("host", Type.A).run();

    assertThat(domainServer.queries(), contains(Record.newRecord(Name.fromString("host.a.test."), Type.A, DClass.IN)));
  }

  @Test
  void shouldRegisterTheRoutingResolverEvenWithoutAConfigurationFile() {
    System.clearProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE);

    DnsControl.install();

    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));
    assertThat(((RoutingResolver) Lookup.getDefaultResolver()).getRoutingTable(), nullValue());
  }

  @Test
  void uninstallWithoutInstallShouldChangeNothing() {
    Resolver resolverBefore = Lookup.getDefaultResolver();

    DnsControl.uninstall();

    assertThat(Lookup.getDefaultResolver(), sameInstance(resolverBefore));
  }

  /** Hosts-file changes are picked up by a background check, so wait for the expected answer instead of racing it. */
  private static InetAddress addressWithinTwoSeconds(String name, InetAddress expected) throws Exception {
    Instant deadline = Instant.now().plusSeconds(2);
    InetAddress address = addressOf(name);
    while (!expected.equals(address) && Instant.now().isBefore(deadline)) {
      Thread.sleep(5);
      address = addressOf(name);
    }
    return address;
  }

  private static InetAddress addressOf(String name) throws Exception {
    Record[] answers = new Lookup(name, Type.A).run();
    return answers == null ? null : ((ARecord) answers[0]).getAddress();
  }

  private static Path hostsFileWith(String line) throws Exception {
    Path hosts = Files.createTempFile("hosts", ".txt");
    Files.writeString(hosts, line + "\n");
    System.setProperty(JDK_NET_HOSTS_FILE, hosts.toString());
    return hosts;
  }
}
