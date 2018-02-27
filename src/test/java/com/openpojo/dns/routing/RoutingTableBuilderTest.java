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

import com.openpojo.dns.exception.RouteSetupException;
import com.openpojo.dns.routing.impl.DefaultRoute;
import com.openpojo.dns.routing.impl.HostRoute;
import com.openpojo.dns.routing.impl.TopLevelDomainRoute;
import com.openpojo.dns.testdouble.dummy.ResolverDummy;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.Resolver;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class RoutingTableBuilderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldCreateEmptyRoutingTable() {
    RoutingTable routingTable = RoutingTableBuilder.create().build();
    assertThat(routingTable, notNullValue());

    assertThat(routingTable.getHostRoutes(), notNullValue());
    assertThat(routingTable.getHostRoutes().size(), is(0));

    assertThat(routingTable.getTopLevelDomainRoutes(), notNullValue());
    assertThat(routingTable.getTopLevelDomainRoutes().size(), is(0));

    assertThat(routingTable.getDefaultRoutes(), notNullValue());
    assertThat(routingTable.getDefaultRoutes().size(), is(0));
  }

  @Test
  public void whenCalledWithHostRouteBuildsWithHostRoute() {
    final HostRoute route = new HostRoute("host.com", new ResolverDummy());
    RoutingTable routingTable = RoutingTableBuilder.create().with(route).build();

    assertThat(routingTable.getHostRoutes().size(), is(1));
    assertThat(routingTable.getHostRoutes().get(0), sameInstance(route));

    assertThat(routingTable.getTopLevelDomainRoutes().size(), is(0));
    assertThat(routingTable.getDefaultRoutes().size(), is(0));
  }

  @Test
  public void whenCalledWithTopLevelDomainRouteBuildsWithTopLevelDomainRoute() {
    final TopLevelDomainRoute route = new TopLevelDomainRoute(".host.com", new ResolverDummy());
    RoutingTable routingTable = RoutingTableBuilder.create().with(route).build();

    assertThat(routingTable.getHostRoutes().size(), is(0));

    assertThat(routingTable.getTopLevelDomainRoutes().size(), is(1));
    assertThat(routingTable.getTopLevelDomainRoutes().get(0), sameInstance(route));

    assertThat(routingTable.getDefaultRoutes().size(), is(0));
  }

  @Test
  public void whenCalledWithDefaultRouteBuildsWithDefaultRoute() {
    final DefaultRoute route = new DefaultRoute(null, new ResolverDummy());
    RoutingTable routingTable = RoutingTableBuilder.create().with(route).build();

    assertThat(routingTable.getHostRoutes().size(), is(0));
    assertThat(routingTable.getTopLevelDomainRoutes().size(), is(0));

    assertThat(routingTable.getDefaultRoutes().size(), is(1));
    assertThat(routingTable.getDefaultRoutes().get(0), sameInstance(route));
  }

  @Test
  public void shouldFailToAddRouteForUnrecognizableType() {
    thrown.expect(RouteSetupException.class);
    final Route anonymousClassRoute = getAnonymousClassRoute();
    thrown.expectMessage("Unrecognized route type [" + anonymousClassRoute.getClass().getName() + "]");

    RoutingTableBuilder.create().with(anonymousClassRoute);
  }

  private Route getAnonymousClassRoute() {
    return new Route() {
      @Override
      public String getKey() {
        return null;
      }

      @Override
      public Resolver[] getResolvers() {
        return new Resolver[0];
      }
    };
  }
}