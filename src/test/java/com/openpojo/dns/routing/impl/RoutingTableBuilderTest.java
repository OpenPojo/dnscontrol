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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;

import static com.openpojo.dns.constants.TestConstants.UNKNOWN_SERVER;
import static com.openpojo.dns.routing.utils.DomainUtils.toDnsDomain;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author oshoukry
 */
public class RoutingTableBuilderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldCreateEmptyRoutingTable() {
    RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();
    assertThat(routingTableBuilder, notNullValue());
    assertThat(routingTableBuilder.getDestinationMap().size(), is(0));
  }

  @Test
  public void shouldCreateOptimizeRoutingTableWhenBuild() {
    RoutingTable routingTable = RoutingTableBuilder.create().build();
    assertThat(routingTable, notNullValue());
  }

  @Test
  public void whenCalledWithNullServerSetsResolverToNoOpResolver() {
    final String destination = "host.com";

    final RoutingTable routingTable = RoutingTableBuilder.create().with(destination, null).build();
    Resolver noOpResolver = routingTable.getResolverFor(destination);
    assertThat(noOpResolver, instanceOf(NoOpResolver.class));
  }

  @Test
  public void whenHostPassedInMapHasOneEntryOptimized() {
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
  public void shouldBuildProperRoutingTable() {
    String destination = "www.openpojo.com";
    List<String> dnsServers = singletonList("127.0.0.1");
    final RoutingTable routingTable = RoutingTableBuilder.create().with(destination, dnsServers).build();
    assertThat(routingTable, notNullValue());
    assertThat(routingTable.getResolverFor(destination), notNullValue());
  }

  @Test
  public void shouldBuildWithDnsConfigReader() {
    Map<String, List<String>> configuration = new HashMap<>();
    final String destination = "www.openpojo.com";
    configuration.put(destination, new ArrayList<String>());

    DnsConfigReaderSpy dnsConfigReaderSpy = new DnsConfigReaderSpy();
    RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();

    dnsConfigReaderSpy.configuration = configuration;
    Assert.assertThat(dnsConfigReaderSpy.getConfigurationCalled, is(false));

    routingTableBuilder.with(dnsConfigReaderSpy);
    assertThat(dnsConfigReaderSpy.getConfigurationCalled, is(true));
    assertThat(routingTableBuilder.getDestinationMap().get(toDnsDomain(destination)), notNullValue());
  }

  @Test
  public void shouldThrowRouteSetupWhenHostIsUnknown() {
    thrown.expect(RouteSetupException.class);
    thrown.expectMessage("Failed to create dns routing map");
    thrown.expectCause(isA(UnknownHostException.class));
    RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();
    routingTableBuilder.with("www.openpojo.com", Collections.singletonList(UNKNOWN_SERVER)).build();
  }

  @Test
  public void shouldBreakOutSystemToResolveConfEntries() {
    final FileDnsConfigReader fileDnsConfigReader = new FileDnsConfigReader("dnscontrol.test.withsystem.conf");
    RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();
    routingTableBuilder.with(fileDnsConfigReader);
    final Map<String, List<String>> destinationMap = routingTableBuilder.getDestinationMap();

    final String[] servers = ResolverConfig.getCurrentConfig().servers();
    assertThat(destinationMap.get(toDnsDomain("system.openpojo.com")), contains(servers));
  }

}