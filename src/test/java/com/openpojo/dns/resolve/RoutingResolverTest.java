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

package com.openpojo.dns.resolve;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.openpojo.dns.DnsControl;
import com.openpojo.dns.cache.CacheControl;
import com.openpojo.dns.constants.TestConstants;
import com.openpojo.dns.routing.RoutingTable;
import com.openpojo.dns.routing.impl.OptimizedRoutingTable;
import com.openpojo.dns.routing.impl.RoutingTableBuilder;
import com.openpojo.dns.testdouble.spy.ResolverSpy;
import com.openpojo.dns.testdouble.spy.ResolverSpyFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.*;

import static com.openpojo.dns.cache.utils.VerificationHelper.verifyCacheIsEmpty;
import static com.openpojo.dns.constants.TestConstants.SERVER_1_DOMAIN;
import static com.openpojo.dns.constants.TestConstants.SERVER_1_IPv4_BYTES;
import static com.openpojo.dns.constants.TestConstants.SERVER_1_NAME;
import static com.openpojo.dns.routing.RoutingTable.DOT;
import static com.openpojo.dns.routing.utils.DomainUtils.toDnsDomain;
import static java.net.InetAddress.getByAddress;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author oshoukry
 */
public class RoutingResolverTest {

  private List<String> implementedMethods = Arrays.asList("setRoutingTable", "getRoutingTable", "send");
  private ResolverSpy spyResolver;

  @Before
  public void setUp() throws Exception {
    spyResolver = ResolverSpyFactory.create(new ExtendedResolver());
    Lookup.refreshDefault();
  }

  @After
  public void teardown() {
    Lookup.refreshDefault();
  }

  @Test
  public void testRoutingTableSetAndGet() {
    RoutingResolver routingResolver = new RoutingResolver(spyResolver);
    assertThat(routingResolver.getRoutingTable(), nullValue());

    RoutingTable routingTable = RoutingTableBuilder.create().build();
    routingResolver.setRoutingTable(routingTable);

    assertThat(routingResolver.getRoutingTable(), sameInstance(routingTable));
  }

  @Test
  public void shouldResolveQeury() throws IOException {
    RoutingResolver routingResolver = new RoutingResolver(spyResolver);
    Record record = Record.newRecord(new Name(TestConstants.SERVER_1_NAME), Type.A, DClass.IN);
    Message query = Message.newQuery(record);
    final Message response = routingResolver.send(query);
    assertThat(response, notNullValue());
    final Record[] answerSection = response.getSectionArray(Section.ANSWER);
    assertThat(answerSection, notNullValue());
    assertThat(answerSection.length, is(1));
    final Record singleRecord = answerSection[0];
    assertThat(singleRecord, instanceOf(ARecord.class));
    assertThat(((ARecord) singleRecord).getAddress(), is(getByAddress(SERVER_1_IPv4_BYTES)));
  }

  @Test
  public void shouldSendQueryToDomainResolver() throws IOException {
    RoutingResolver routingResolver = new RoutingResolver(new SimpleResolver());
    HashMap<String, Resolver> compiledMap = new HashMap<>();

    compiledMap.put(toDnsDomain(DOT + SERVER_1_DOMAIN), spyResolver);
    compiledMap.put(DOT, new SimpleResolver());

    OptimizedRoutingTable table = new OptimizedRoutingTable(compiledMap);
    routingResolver.setRoutingTable(table);

    assertThat(table.getResolverFor(DOT), notNullValue());
    assertThat(table.getResolverFor(SERVER_1_NAME), sameInstance((Resolver) spyResolver));

    Record record = Record.newRecord(new Name(TestConstants.SERVER_1_NAME), Type.A, DClass.IN);
    Message query = Message.newQuery(record);
    final Message response = routingResolver.send(query);
    assertThat(response, notNullValue());
    final Record[] answerSection = response.getSectionArray(Section.ANSWER);
    assertThat(answerSection, notNullValue());
    assertThat(answerSection.length, is(1));
    final Record singleRecord = answerSection[0];
    assertThat(singleRecord, instanceOf(ARecord.class));
    assertThat(((ARecord) singleRecord).getAddress(), is(getByAddress(SERVER_1_IPv4_BYTES)));
    final List<String> calls = spyResolver.getCalls();
    assertThat(calls.size(), is(1));
    assertThat(calls.get(0), startsWith("send([;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: "));
    assertThat(calls.get(0), endsWith(
        "\n"
            + ";; flags: rd ; qd: 1 an: 0 au: 0 ad: 0 \n"
            + ";; QUESTIONS:\n"
            + ";;\t" + SERVER_1_NAME + ", type = A, class = IN\n"
            + "\n"
            + ";; ANSWERS:\n"
            + "\n"
            + ";; AUTHORITY RECORDS:\n"
            + "\n"
            + ";; ADDITIONAL RECORDS:\n"
            + "\n"
            + ";; Message size: 0 bytes])"));
  }

  @Test
  public void whenCacheIsClearCallsFlowToResolver() throws TextParseException {
    HashMap<String, Resolver> compiledMap = new HashMap<>();

    compiledMap.put(toDnsDomain(DOT + SERVER_1_DOMAIN), spyResolver);

    OptimizedRoutingTable table = new OptimizedRoutingTable(compiledMap);
    DnsControl.getInstance().setRoutingTable(table);
    DnsControl.getInstance().registerRoutingResolver();

    verifyCacheIsEmpty();

    new Lookup(SERVER_1_NAME, Type.A).run(); // 1st call
    new Lookup(SERVER_1_NAME, Type.A).run(); // from cache
    assertThat(spyResolver.getCalls().size(), is(1));

    CacheControl.resetCache();
    verifyCacheIsEmpty();

    new Lookup(SERVER_1_NAME, Type.A).run(); // 2nd call
    assertThat(spyResolver.getCalls().size(), is(2));
  }


  @Test
  public void shouldThrowUnImplementedOnAllMethods() {
    NotSupportedMethodsValidator.validateMethodsNotImplemented(RoutingResolver.class, implementedMethods);
  }
}
