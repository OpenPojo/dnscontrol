/*
 * Copyright (c) 2018-2026 Osman Shoukry
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

package com.openpojo.dns.resolve;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openpojo.dns.DnsControl;
import com.openpojo.dns.cache.CacheControl;
import com.openpojo.dns.routing.RoutingTable;
import com.openpojo.dns.routing.impl.OptimizedRoutingTable;
import com.openpojo.dns.routing.impl.RoutingTableBuilder;
import com.openpojo.dns.testing.FakeDnsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import static com.openpojo.dns.cache.utils.VerificationHelper.verifyCacheIsEmpty;
import static com.openpojo.dns.routing.RoutingTable.DOT;
import static com.openpojo.dns.routing.utils.DomainUtils.toDnsDomain;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

class RoutingResolverTest {
  private static final String ROUTED_DOMAIN = "a.test.";
  private static final Name ROUTED_HOST = name("host." + ROUTED_DOMAIN);
  private static final Name OTHER_HOST = name("host.other.test.");

  private final List<String> implementedMethods = Arrays.asList("setRoutingTable", "getRoutingTable", "send");

  private FakeDnsServer defaultServer;
  private FakeDnsServer routedServer;
  private InetAddress routedAddress;
  private InetAddress defaultAddress;

  @BeforeEach
  void startServers() throws Exception {
    defaultServer = FakeDnsServer.start();
    routedServer = FakeDnsServer.start();
    routedAddress = InetAddress.getByName("10.1.0.1");
    defaultAddress = InetAddress.getByName("10.2.0.1");
    routedServer.addRecord(new ARecord(ROUTED_HOST, DClass.IN, 300, routedAddress));
    defaultServer.addRecord(new ARecord(ROUTED_HOST, DClass.IN, 300, defaultAddress));
    defaultServer.addRecord(new ARecord(OTHER_HOST, DClass.IN, 300, defaultAddress));
    Lookup.refreshDefault();
  }

  @AfterEach
  void stopServers() {
    Lookup.refreshDefault();
    routedServer.close();
    defaultServer.close();
  }

  @Test
  void shouldHoldTheRoutingTableItIsGiven() throws Exception {
    RoutingResolver routingResolver = new RoutingResolver(resolverFor(defaultServer));
    assertThat(routingResolver.getRoutingTable(), nullValue());

    RoutingTable routingTable = RoutingTableBuilder.create().build();
    routingResolver.setRoutingTable(routingTable);

    assertThat(routingResolver.getRoutingTable(), sameInstance(routingTable));
  }

  @Test
  void shouldUseTheDefaultResolverWhenNoRoutingTableIsSet() throws Exception {
    RoutingResolver routingResolver = new RoutingResolver(resolverFor(defaultServer));

    Message response = routingResolver.send(queryFor(ROUTED_HOST));

    assertThat(answeredAddress(response), is(defaultAddress));
    assertThat(defaultServer.queries(), contains(question(ROUTED_HOST)));
    assertThat(routedServer.queries(), empty());
  }

  @Test
  void shouldSendQueriesForARoutedDomainToThatDomainsResolver() throws Exception {
    RoutingResolver routingResolver = new RoutingResolver(resolverFor(defaultServer));
    routingResolver.setRoutingTable(tableRouting(ROUTED_DOMAIN, routedServer, defaultServer));

    Message response = routingResolver.send(queryFor(ROUTED_HOST));

    assertThat(answeredAddress(response), is(routedAddress));
    assertThat(routedServer.queries(), contains(question(ROUTED_HOST)));
    assertThat(defaultServer.queries(), empty());
  }

  @Test
  void shouldSendQueriesOutsideRoutedDomainsToTheDefaultRoute() throws Exception {
    RoutingResolver routingResolver = new RoutingResolver(resolverFor(defaultServer));
    routingResolver.setRoutingTable(tableRouting(ROUTED_DOMAIN, routedServer, defaultServer));

    Message response = routingResolver.send(queryFor(OTHER_HOST));

    assertThat(answeredAddress(response), is(defaultAddress));
    assertThat(defaultServer.queries(), contains(question(OTHER_HOST)));
    assertThat(routedServer.queries(), empty());
  }

  @Test
  void shouldAnswerFromCacheUntilTheCacheIsReset() throws Exception {
    DnsControl.getInstance().setRoutingTable(tableRouting(ROUTED_DOMAIN, routedServer, defaultServer));
    DnsControl.getInstance().registerRoutingResolver();
    verifyCacheIsEmpty();

    new Lookup(ROUTED_HOST, Type.A).run();
    new Lookup(ROUTED_HOST, Type.A).run();
    assertThat(routedServer.queries().size(), is(1));

    CacheControl.resetCache();
    verifyCacheIsEmpty();

    new Lookup(ROUTED_HOST, Type.A).run();
    assertThat(routedServer.queries().size(), is(2));
  }

  @Test
  void shouldThrowUnImplementedOnAllMethods() {
    NotSupportedMethodsValidator.validateMethodsNotImplemented(RoutingResolver.class, implementedMethods);
  }

  private static RoutingTable tableRouting(String domain, FakeDnsServer domainServer, FakeDnsServer defaultServer) {
    Map<String, Resolver> compiledMap = new HashMap<>();
    compiledMap.put(toDnsDomain(DOT + domain), resolverFor(domainServer));
    compiledMap.put(DOT, resolverFor(defaultServer));
    return new OptimizedRoutingTable(compiledMap);
  }

  private static Resolver resolverFor(FakeDnsServer server) {
    return new SimpleResolver(server.address());
  }

  private static Message queryFor(Name host) {
    return Message.newQuery(question(host));
  }

  private static Record question(Name host) {
    return Record.newRecord(host, Type.A, DClass.IN);
  }

  private static InetAddress answeredAddress(Message response) {
    Record[] answers = response.getSectionArray(Section.ANSWER);
    assertThat(answers.length, is(1));
    assertThat(answers[0], instanceOf(ARecord.class));
    return ((ARecord) answers[0]).getAddress();
  }

  private static Name name(String value) {
    try {
      return Name.fromString(value);
    } catch (Exception e) {
      throw new IllegalArgumentException(value, e);
    }
  }
}
