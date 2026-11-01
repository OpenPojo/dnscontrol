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

import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.testing.FakeDnsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

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
    domainServer = FakeDnsServer.start();
    Path config = Files.createTempFile("dnscontrol", ".conf");
    Files.writeString(config, ".a.test=" + domainServer.hostAndPort() + "\n");
    System.setProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE, config.toString());
  }

  @AfterEach
  void restoreDnsjavaDefaults() {
    System.clearProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE);
    System.clearProperty(JDK_NET_HOSTS_FILE);
    System.clearProperty(DnsControl.DNSJAVA_HOSTS_FILE_CHECK_INTERVAL);
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
    Record[] answers = new Lookup("server50.a.test.", Type.A).run();

    assertThat(((ARecord) answers[0]).getAddress(), is(InetAddress.getByName("10.0.0.6")));
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

  private static Path hostsFileWith(String line) throws Exception {
    Path hosts = Files.createTempFile("hosts", ".txt");
    Files.writeString(hosts, line + "\n");
    System.setProperty(JDK_NET_HOSTS_FILE, hosts.toString());
    return hosts;
  }
}
