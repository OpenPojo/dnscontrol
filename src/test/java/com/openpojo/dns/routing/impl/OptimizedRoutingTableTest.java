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
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

import static com.openpojo.dns.constants.TestConstants.SERVER_1_DOMAIN;
import static com.openpojo.dns.constants.TestConstants.SERVER_1_NAME;
import static com.openpojo.dns.routing.RoutingTable.DOT;
import static com.openpojo.dns.routing.utils.DomainUtils.toDnsDomain;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

/**
 * @author oshoukry
 */
public class OptimizedRoutingTableTest {

  private Resolver resolver;
  private HashMap<String, Resolver> compiledMap;

  @Before
  public void setup() throws UnknownHostException {
    resolver = new SimpleResolver();
    compiledMap = new HashMap<>();
  }

  @Test
  public void whenEmptyReturnNull() {
    OptimizedRoutingTable table = new OptimizedRoutingTable(null);
    assertThat(table.getResolverFor(DOT), nullValue());
  }

  @Test
  public void whenMatchingHostReturnResolver() {
    compiledMap.put(toDnsDomain(SERVER_1_NAME), resolver);

    OptimizedRoutingTable table = new OptimizedRoutingTable(compiledMap);
    assertThat(table.getResolverFor(DOT), nullValue());
    assertThat(table.getResolverFor(SERVER_1_NAME), sameInstance(resolver));
  }

  @Test
  public void whenNoMatchingHostAndDefaultRouteReturnDefault() {
    compiledMap.put(DOT, resolver);

    OptimizedRoutingTable table = new OptimizedRoutingTable(compiledMap);
    assertThat(table.getResolverFor(DOT), sameInstance(resolver));
    assertThat(table.getResolverFor(SERVER_1_NAME), sameInstance(resolver));
  }

  @Test
  public void whenNoMatchingHostAndDomainMatchReturnDomainResolver() throws UnknownHostException {
    compiledMap.put(toDnsDomain(DOT + SERVER_1_DOMAIN), resolver);
    compiledMap.put(DOT, new SimpleResolver());

    OptimizedRoutingTable table = new OptimizedRoutingTable(compiledMap);
    assertThat(table.getResolverFor(DOT), notNullValue());
    assertThat(table.getResolverFor(SERVER_1_NAME), sameInstance(resolver));
  }

  @Test
  public void shouldBeCaseInsensitive() {
    compiledMap.put(toDnsDomain(SERVER_1_NAME.toLowerCase()), resolver);
    OptimizedRoutingTable table = new OptimizedRoutingTable(compiledMap);

    assertThat(table.getResolverFor(SERVER_1_NAME.toUpperCase()), is(resolver));
  }
}