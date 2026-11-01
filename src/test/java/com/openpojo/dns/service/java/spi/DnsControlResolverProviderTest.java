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

package com.openpojo.dns.service.java.spi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolver.LookupPolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.openpojo.dns.DnsControl;
import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.resolve.RoutingResolver;
import com.openpojo.dns.testing.FakeDnsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.ReverseMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The JEP 418 provider: the JDK asks it once, on the first InetAddress lookup, for the system-wide resolver. The sealed
 * Configuration type cannot be stubbed, so these tests go through the package-private factory that get() adapts to;
 * the cold-JVM integration test covers get() itself.
 */
class DnsControlResolverProviderTest {
  private static final String LOCAL_HOST_NAME = "this-machine";

  private FakeDnsServer domainServer;
  private final Name routedHost = name("host.a.test.");
  private InetAddress routedV4;
  private InetAddress routedV6;
  private final List<String> builtinAskedFor = new ArrayList<>();

  @BeforeEach
  void routeADomainToAFakeServer() throws Exception {
    Lookup.refreshDefault();
    // The JDK may already have loaded the provider (and installed DNSControl) earlier in this JVM; start clean.
    DnsControl.recreateInstance();
    domainServer = FakeDnsServer.start();
    routedV4 = InetAddress.getByName("10.4.4.4");
    routedV6 = InetAddress.getByName("2001:db8::4");
    domainServer.addRecord(new ARecord(routedHost, DClass.IN, 300, routedV4));
    domainServer.addRecord(new AAAARecord(routedHost, DClass.IN, 300, routedV6));
    domainServer.addRecord(new PTRRecord(ReverseMap.fromAddress(routedV4), DClass.IN, 300, routedHost));
    Path config = Files.createTempFile("dnscontrol", ".conf");
    Files.writeString(config, ".a.test=" + domainServer.hostAndPort() + "\n.10.in-addr.arpa=" + domainServer.hostAndPort() + "\n");
    System.setProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE, config.toString());
  }

  @AfterEach
  void restoreDnsjava() {
    DnsControl.uninstall();
    System.clearProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE);
    Lookup.refreshDefault();
    DnsControl.recreateInstance();
    domainServer.close();
  }

  @Test
  void shouldBeNamedDnscontrol() {
    assertThat(new DnsControlResolverProvider().name(), is("dnscontrol"));
  }

  @Test
  void shouldInstallDnsControlAndResolveIpv4ThroughTheRoutingTable() throws Exception {
    InetAddressResolver resolver = resolver();

    List<InetAddress> addresses = resolver.lookupByName(routedHost.toString(), LookupPolicy.of(LookupPolicy.IPV4)).toList();

    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));
    assertThat(addresses, contains(routedV4));
  }

  @Test
  void shouldResolveIpv6WhenThePolicyAsksForIt() throws Exception {
    List<InetAddress> addresses = resolver().lookupByName(routedHost.toString(), LookupPolicy.of(LookupPolicy.IPV6)).toList();

    assertThat(addresses, contains(routedV6));
  }

  @Test
  void shouldOrderAddressesAsThePolicyPrefers() throws Exception {
    InetAddressResolver resolver = resolver();

    assertThat(resolver.lookupByName(routedHost.toString(), LookupPolicy.of(LookupPolicy.IPV4 | LookupPolicy.IPV6 | LookupPolicy.IPV6_FIRST)).toList(),
        contains(routedV6, routedV4));
    assertThat(resolver.lookupByName(routedHost.toString(), LookupPolicy.of(LookupPolicy.IPV4 | LookupPolicy.IPV6 | LookupPolicy.IPV4_FIRST)).toList(),
        contains(routedV4, routedV6));
  }

  @Test
  void shouldThrowUnknownHostForANameNoServerKnows() {
    UnknownHostException thrown = assertThrows(UnknownHostException.class,
        () -> resolver().lookupByName("missing.a.test.", LookupPolicy.of(LookupPolicy.IPV4)));

    assertThat(thrown.getMessage(), containsString("missing.a.test."));
  }

  @Test
  void shouldLeaveLocalhostAndTheMachineNameToTheBuiltInResolver() throws Exception {
    InetAddressResolver resolver = resolver();

    resolver.lookupByName("localhost", LookupPolicy.of(LookupPolicy.IPV4));
    resolver.lookupByName(LOCAL_HOST_NAME, LookupPolicy.of(LookupPolicy.IPV4));

    assertThat(builtinAskedFor, contains("localhost", LOCAL_HOST_NAME));
    assertThat(domainServer.queries().size(), is(0));
  }

  @Test
  void shouldReverseLookupThroughTheRoutingTable() throws Exception {
    assertThat(resolver().lookupByAddress(routedV4.getAddress()), is(routedHost.toString()));
  }

  @Test
  void shouldThrowUnknownHostForAnAddressWithNoPtrRecord() {
    assertThrows(UnknownHostException.class, () -> resolver().lookupByAddress(InetAddress.getByName("10.5.5.5").getAddress()));
  }

  @Test
  void shouldHandBackTheBuiltInResolverWhenDisabled() {
    System.setProperty(DnsControl.ENABLED, "false");
    try {
      assertThat(resolver(), sameInstance(builtin));
      assertThat(Lookup.getDefaultResolver(), not(instanceOf(RoutingResolver.class)));
    } finally {
      System.clearProperty(DnsControl.ENABLED);
    }
  }

  private InetAddressResolver resolver() {
    return DnsControlResolverProvider.resolver(() -> builtin, () -> LOCAL_HOST_NAME);
  }

  private final InetAddressResolver builtin = new InetAddressResolver() {
    @Override
    public Stream<InetAddress> lookupByName(String host, LookupPolicy lookupPolicy) {
      builtinAskedFor.add(host);
      return Stream.of(InetAddress.getLoopbackAddress());
    }

    @Override
    public String lookupByAddress(byte[] addr) throws UnknownHostException {
      throw new UnknownHostException("built-in resolver asked for reverse lookup");
    }
  };

  private static Name name(String value) {
    try {
      return Name.fromString(value);
    } catch (Exception e) {
      throw new IllegalArgumentException(value, e);
    }
  }
}
