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

import com.openpojo.dns.exception.RouteException;
import com.openpojo.dns.routing.impl.DefaultRoute;
import com.openpojo.dns.routing.impl.TopLevelDomainRoute;
import com.openpojo.dns.routing.impl.HostRoute;
import com.openpojo.dns.testdouble.spy.ResolverSpy;
import com.openpojo.dns.testdouble.spy.ResolverSpyFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

import static com.openpojo.dns.routing.RouteFactory.createRoute;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class RouteFactoryTest {
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

    thrown.expect(RouteException.class);
    thrown.expectMessage("Invalid call, can't create routing without resolvers for [" + destination + "]");

    createRoute(destination, (Resolver[])null);
  }

  @Test
  @SuppressWarnings("RedundantArrayCreation")
  public void shouldThrowExceptionIfResolversEmpty() {
    final String destination = "domain.com.";

    thrown.expect(RouteException.class);
    thrown.expectMessage("Invalid call, can't create routing without resolvers for [" + destination + "]");

    createRoute(destination, new Resolver[0]);
  }

  @Test
  public void shouldGetHostResolver() throws UnknownHostException {
    verifyReturnedType("domain.com.", HostRoute.class);
  }

  @Test
  public void whenNullDestinationCreateDefaultRoutingTableEntry() {
    verifyReturnedType(null, DefaultRoute.class);
  }

  @Test
  public void whenEmptyDestinationCreateDefaultRoutingTableEntry() throws UnknownHostException {
    verifyReturnedType("", DefaultRoute.class);
  }

  @Test
  public void whenStartsWithDotReturnDomainRoutingTableEntry() {
    verifyReturnedType(".domain.com", TopLevelDomainRoute.class);
  }

  private void verifyReturnedType(String destination, Class<? extends Route> expectedType) {
    final Route route = createRoute(destination, resolverSpy);

    assertThat(route, notNullValue());
    assertThat(resolverSpy.getCalls().size(), is(0));
    assertThat(route, instanceOf(expectedType));
  }

}