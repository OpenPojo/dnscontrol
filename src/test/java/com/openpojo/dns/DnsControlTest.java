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
import java.net.UnknownHostException;

import com.openpojo.dns.cache.CacheControl;
import com.openpojo.dns.cache.utils.VerificationHelper;
import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.config.impl.EnvironmentDnsConfigReader;
import com.openpojo.dns.exception.RouteSetupException;
import com.openpojo.dns.resolve.RoutingResolver;
import com.openpojo.dns.testing.FakeDnsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import static com.openpojo.dns.DnsControl.recreateInstance;
import static com.openpojo.dns.constants.TestConstants.UNKNOWN_SERVER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.*;

/**
 * @author oshoukry
 */
class DnsControlTest {

  private final DnsControl dnsControl = DnsControl.getInstance();
  private Resolver defaultResolver;

  @BeforeEach
  void setup() {
    System.clearProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY);
    Lookup.refreshDefault();
    CacheControl.resetCache();
    defaultResolver = Lookup.getDefaultResolver();
  }

  @AfterEach
  void teardown() {
    System.clearProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY);
    Lookup.refreshDefault();
    CacheControl.resetCache();
  }

  @Test
  void canGetInstance() {
    assertThat(dnsControl, notNullValue());
  }

  @Test
  void canRegister() {
    assertThat(defaultResolver, not(instanceOf(RoutingResolver.class)));
    assertThat(dnsControl.isRoutingResolverRegistered(), is(false));

    dnsControl.registerRoutingResolver();
    assertThat(dnsControl.isRoutingResolverRegistered(), is(true));

    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));
  }

  @Test
  void canUnRegister() {
    dnsControl.registerRoutingResolver();
    assertThat(dnsControl.isRoutingResolverRegistered(), is(true));
    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));

    dnsControl.unRegisterRoutingResolver();
    assertThat(defaultResolver, not(instanceOf(RoutingResolver.class)));
    assertThat(dnsControl.isRoutingResolverRegistered(), is(false));
  }

  @Test
  void canResolveSomeEntry() throws Exception {
    try (FakeDnsServer server = FakeDnsServer.start()) {
      Name host = Name.fromString("host.other.test.");
      InetAddress address = InetAddress.getByName("10.9.9.9");
      server.addRecord(new ARecord(host, DClass.IN, 300, address));
      DnsControl control = pointDefaultRouteAt(server);
      control.registerRoutingResolver();

      assertThat(((ARecord) new Lookup(host, Type.A).run()[0]).getAddress(), is(address));

      control.registerRoutingResolver();
      assertThat(((ARecord) new Lookup(host, Type.A).run()[0]).getAddress(), is(address));
    } finally {
      restoreSystemDefaultRoute();
    }
  }

  @Test
  void whenChangingRoutingTableAutoRegisterRoutingResolver() {
    assertThat(dnsControl.isRoutingResolverRegistered(), is(false));
    assertThat(Lookup.getDefaultResolver(), not(instanceOf(RoutingResolver.class)));
    dnsControl.setRoutingTable(null);
    assertThat(dnsControl.isRoutingResolverRegistered(), is(true));
    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));
  }

  @Test
  void shouldClearCacheWheneverRoutingTableChanges() throws Exception {
    try (FakeDnsServer server = FakeDnsServer.start()) {
      Name host = Name.fromString("host.other.test.");
      server.addRecord(new ARecord(host, DClass.IN, 300, InetAddress.getByName("10.9.9.9")));
      DnsControl control = pointDefaultRouteAt(server);
      control.registerRoutingResolver();
      VerificationHelper.verifyCacheIsEmpty();

      new Lookup(host, Type.A).run();
      assertThat(Lookup.getDefaultCache(DClass.IN).getSize(), is(1));

      control.setRoutingTable(null);
      VerificationHelper.verifyCacheIsEmpty();
    } finally {
      restoreSystemDefaultRoute();
    }
  }

  @Test
  void shouldThrowResolverException() {
        System.setProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY, UNKNOWN_SERVER);

    RouteSetupException thrown = assertThrows(RouteSetupException.class, () -> recreateInstance());
    assertThat(thrown.getMessage(), containsString("Unknown host defined in configuration reader [" + EnvironmentDnsConfigReader.class.getName() + "] " + UNKNOWN_SERVER));
    assertThat(thrown.getCause(), isA(java.net.UnknownHostException.class));
  }

  @Test
  void whenRecreateGetInstanceShouldBeDifferent() {
    DnsControl instance = DnsControl.getInstance();
    assertThat(instance, sameInstance(DnsControl.getInstance()));
    DnsControl differentInstance = DnsControl.recreateInstance();
    assertThat(differentInstance, not(sameInstance(instance)));
    assertThat(differentInstance, sameInstance(DnsControl.getInstance()));
  }

  @Test
  void shouldSendUnroutedQueriesToTheConfiguredDefaultServers() throws Exception {
    try (FakeDnsServer defaultServer = FakeDnsServer.start()) {
      pointDefaultRouteAt(defaultServer).registerRoutingResolver();
      Record question = Record.newRecord(Name.fromString("host.other.test."), Type.A, DClass.IN);

      new Lookup(question.getName(), Type.A).run();

      assertThat(defaultServer.queries(), contains(question));
    } finally {
      restoreSystemDefaultRoute();
    }
  }

  private static DnsControl pointDefaultRouteAt(FakeDnsServer server) {
    System.setProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY, server.hostAndPort());
    return recreateInstance();
  }

  private static void restoreSystemDefaultRoute() {
    System.clearProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY);
    recreateInstance();
  }
}
