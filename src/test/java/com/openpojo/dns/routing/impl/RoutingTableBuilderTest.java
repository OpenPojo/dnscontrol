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

package com.openpojo.dns.routing.impl;

import com.openpojo.dns.config.utils.ServerAddressParser;
import org.xbill.DNS.Type;
import org.xbill.DNS.Record;
import org.xbill.DNS.Name;
import org.xbill.DNS.Message;
import org.xbill.DNS.DClass;
import com.openpojo.dns.testing.FakeDnsServer;
import com.openpojo.dns.config.impl.NoOpDnsConfigReader;
import java.util.Arrays;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openpojo.dns.config.impl.FileDnsConfigReader;
import com.openpojo.dns.exception.RouteSetupException;
import com.openpojo.dns.resolve.NoOpResolver;
import com.openpojo.dns.routing.RoutingTable;
import com.openpojo.dns.testdouble.spy.DnsConfigReaderSpy;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;

import static com.openpojo.dns.constants.TestConstants.UNKNOWN_SERVER;
import static com.openpojo.dns.routing.utils.DomainUtils.toDnsDomain;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.*;

/**
 * @author oshoukry
 */
class RoutingTableBuilderTest {

  @Test
  void shouldCreateEmptyRoutingTable() {
    RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();
    assertThat(routingTableBuilder, notNullValue());
    assertThat(routingTableBuilder.getDestinationMap().size(), is(0));
  }

  @Test
  void shouldCreateOptimizeRoutingTableWhenBuild() {
    RoutingTable routingTable = RoutingTableBuilder.create().build();
    assertThat(routingTable, notNullValue());
  }

  @Test
  void whenCalledWithNullServerSetsResolverToNoOpResolver() {
    final String destination = "host.com";

    final RoutingTable routingTable = RoutingTableBuilder.create().with(destination, null).build();
    Resolver noOpResolver = routingTable.getResolverFor(destination);
    assertThat(noOpResolver, instanceOf(NoOpResolver.class));
  }

  @Test
  void whenHostPassedInMapHasOneEntryOptimized() {
    String destination = "host.com";
    List<String> dnsServers = singletonList("127.0.0.1");
    final RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create().with(destination, dnsServers);

    final Map<String, List<String>> destinationMap = routingTableBuilder.getDestinationMap();
    assertThat(destinationMap, notNullValue());
    assertThat(destinationMap.size(), is(1));

    String hierarchicalDestination = destinationMap.keySet().iterator().next();
    assertThat(hierarchicalDestination, is(toDnsDomain(destination)));
    final List<String> actualDnsServers = destinationMap.get(hierarchicalDestination);
    assertThat(actualDnsServers, notNullValue());
    assertThat(actualDnsServers.size(), is(1));
    assertThat(actualDnsServers.get(0), is(dnsServers.get(0)));
  }

  @Test
  void shouldBuildProperRoutingTable() {
    String destination = "www.openpojo.com";
    List<String> dnsServers = singletonList("127.0.0.1");
    final RoutingTable routingTable = RoutingTableBuilder.create().with(destination, dnsServers).build();
    assertThat(routingTable, notNullValue());
    assertThat(routingTable.getResolverFor(destination), notNullValue());
  }

  @Test
  void shouldBuildWithDnsConfigReader() {
    Map<String, List<String>> configuration = new HashMap<>();
    final String destination = "www.openpojo.com";
    configuration.put(destination, new ArrayList<String>());

    DnsConfigReaderSpy dnsConfigReaderSpy = new DnsConfigReaderSpy();
    RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();

    dnsConfigReaderSpy.configuration = configuration;
    assertThat(dnsConfigReaderSpy.getConfigurationCalled, is(false));

    routingTableBuilder.with(dnsConfigReaderSpy);
    assertThat(dnsConfigReaderSpy.getConfigurationCalled, is(true));
    assertThat(routingTableBuilder.getDestinationMap().get(toDnsDomain(destination)), notNullValue());
  }

  @Test
  void shouldThrowRouteSetupWhenHostIsUnknown() {
        RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();

    RouteSetupException thrown = assertThrows(RouteSetupException.class, () -> routingTableBuilder.with("www.openpojo.com", Collections.singletonList(UNKNOWN_SERVER)).build());
    assertThat(thrown.getMessage(), containsString("Failed to create dns routing map"));
    assertThat(thrown.getCause(), isA(UnknownHostException.class));
  }

  @Test
  void shouldBreakOutSystemToResolveConfEntries() {
    final FileDnsConfigReader fileDnsConfigReader = new FileDnsConfigReader("dnscontrol.test.withsystem.conf");
    RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();
    routingTableBuilder.with(fileDnsConfigReader);
    final Map<String, List<String>> destinationMap = routingTableBuilder.getDestinationMap();

    final String[] servers = ResolverConfig.getCurrentConfig().servers().stream()
        .map(ServerAddressParser::format).toArray(String[]::new);
    assertThat(destinationMap.get(toDnsDomain("system.openpojo.com")), contains(servers));
  }

  @Test
  void withReaderShouldReturnTheSameBuilderForChaining() {
    RoutingTableBuilder builder = RoutingTableBuilder.create();
    assertThat(builder.with(new NoOpDnsConfigReader()), sameInstance(builder));
  }

  @Test
  void shouldIgnoreEmptyServerEntries() {
    RoutingTableBuilder builder = RoutingTableBuilder.create();
    builder.with("host.test", Arrays.asList("", "10.0.0.1"));
    assertThat(builder.getDestinationMap().get(toDnsDomain("host.test")), contains("10.0.0.1"));
  }

  @Test
  void shouldRouteToAServerGivenAsHostAndPort() throws Exception {
    try (FakeDnsServer server = FakeDnsServer.start()) {
      RoutingTable table = RoutingTableBuilder.create().with(".a.test", singletonList(server.hostAndPort())).build();
      Record question = Record.newRecord(Name.fromString("x.a.test."), Type.A, DClass.IN);

      table.getResolverFor("x.a.test.").send(Message.newQuery(question));

      assertThat(server.queries(), contains(question));
    }
  }
}
