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

import java.net.UnknownHostException;

import com.openpojo.dns.exception.RoutingTableException;
import com.openpojo.dns.routing.impl.DefaultRoutingTableEntry;
import com.openpojo.dns.routing.impl.DomainRoutingTableEntry;
import com.openpojo.dns.routing.impl.HostRoutingTableEntry;
import com.openpojo.dns.testdouble.spy.ResolverSpy;
import com.openpojo.dns.testdouble.spy.ResolverSpyFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

import static com.openpojo.dns.routing.RoutingTableFactory.createRoutingTableEntry;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class RoutingTableFactoryTest {

  private ResolverSpy resolverSpy;

  @Rule
  public ExpectedException thrown = ExpectedException.none();


  @Before
  public void setUp() throws Exception {
    resolverSpy = ResolverSpyFactory.create(new SimpleResolver());
  }

  @Test
  public void shouldThrowExceptionIfResolversNull() {
    final String destination = "domain.com.";

    thrown.expect(RoutingTableException.class);
    thrown.expectMessage("Invalid call, can't create routing without resolvers for [" + destination + "]");

    createRoutingTableEntry(destination, (Resolver[])null);
  }


  @Test
  @SuppressWarnings("RedundantArrayCreation")
  public void shouldThrowExceptionIfResolversEmpty() {
    final String destination = "domain.com.";

    thrown.expect(RoutingTableException.class);
    thrown.expectMessage("Invalid call, can't create routing without resolvers for [" + destination + "]");

    createRoutingTableEntry(destination, new Resolver[0]);
  }

  @Test
  public void shouldGetHostResolver() throws UnknownHostException {
    verifyReturnedType("domain.com.", HostRoutingTableEntry.class);
  }

  @Test
  public void whenNullDestionationCreateDefaultRoutingTableEntry() {
    verifyReturnedType(null, DefaultRoutingTableEntry.class);
  }

  @Test
  public void whenEmptyDestinationCreateDefaultRoutingTableEntry() throws UnknownHostException {
    verifyReturnedType("", DefaultRoutingTableEntry.class);
  }

  @Test
  public void whenStartsWithDotReturnDomainRoutingTableEntry() {
    verifyReturnedType(".domain.com", DomainRoutingTableEntry.class);
  }

  private void verifyReturnedType(String destination, Class<? extends RoutingTableEntry> expectedType) {
    final RoutingTableEntry routingTableEntry = createRoutingTableEntry(destination, resolverSpy);

    assertThat(routingTableEntry, notNullValue());
    assertThat(resolverSpy.getCalls().size(), is(0));
    assertThat(routingTableEntry, instanceOf(expectedType));
  }

}