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

package com.openpojo.dns.routing;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.openpojo.dns.constants.TestConstants;
import com.openpojo.dns.exception.RoutingResolverException;
import com.openpojo.dns.routing.impl.OptimizedRoutingTable;
import com.openpojo.dns.routing.impl.RoutingTableBuilder;
import com.openpojo.dns.testdouble.spy.ResolverSpy;
import com.openpojo.dns.testdouble.spy.ResolverSpyFactory;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoMethod;
import com.openpojo.reflection.PojoParameter;
import com.openpojo.reflection.exception.ReflectionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.*;

import static com.openpojo.dns.constants.TestConstants.SERVER_1_DOMAIN;
import static com.openpojo.dns.constants.TestConstants.SERVER_1_IPv4_BYTES;
import static com.openpojo.dns.constants.TestConstants.SERVER_1_NAME;
import static com.openpojo.dns.routing.RoutingTable.DOT;
import static com.openpojo.dns.routing.utils.DomainUtils.toDnsDomain;
import static com.openpojo.random.RandomFactory.getRandomValue;
import static com.openpojo.reflection.impl.PojoClassFactory.getPojoClass;
import static java.net.InetAddress.getByAddress;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class RoutingResolverTest {

  private List<String> implementedMethods = Arrays.asList("setRoutingTable", "getRoutingTable", "send");
  private ResolverSpy spyResolver;

  @Before
  public void setUp() throws Exception {
    spyResolver = ResolverSpyFactory.create(new ExtendedResolver());
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
    assertThat(table.getResolverFor(SERVER_1_NAME), sameInstance((Resolver)spyResolver));

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
    assertThat(calls.get(0), endsWith("\n" +
        ";; flags: rd ; qd: 1 an: 0 au: 0 ad: 0 \n" +
        ";; QUESTIONS:\n" +
        ";;\ta.root-servers.net., type = A, class = IN\n" +
        "\n" +
        ";; ANSWERS:\n" +
        "\n" +
        ";; AUTHORITY RECORDS:\n" +
        "\n" +
        ";; ADDITIONAL RECORDS:\n" +
        "\n" +
        ";; Message size: 0 bytes])"));
  }

  @Test
  public void shouldThrowUnImplementedOnAllMethods() {
    PojoClass pojoClass = getPojoClass(RoutingResolver.class);
    RoutingResolver routingResolver = new RoutingResolver(spyResolver);
    for (PojoMethod method : pojoClass.getPojoMethods()) {
      if (!method.isSynthetic() && !method.isConstructor() && !implementedMethods.contains(method.getName()))
        try {
          method.invoke(routingResolver, getRandomParameters(method));
          Assert.fail("Failed to throw expected exception on method [" + method.getName() + "]");
        } catch (ReflectionException re) {
          RoutingResolverException rre = (RoutingResolverException) re.getCause().getCause();
          assertThat(rre.getMessage(), is("Operation not supported"));
        }
    }
  }

  private Object[] getRandomParameters(PojoMethod method) {
    final List<PojoParameter> pojoParameters = method.getPojoParameters();
    Object[] parameters = new Object[pojoParameters.size()];
    for (int i = 0; i < parameters.length; i++) {
      if (pojoParameters.get(i).getType().isPrimitive()) {
        parameters[i] = getRandomValue(pojoParameters.get(i));
      } else {
        parameters[i] = null;
      }
    }
    return parameters;
  }
}