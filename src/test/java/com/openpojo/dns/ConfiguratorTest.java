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

import com.openpojo.dns.routing.RoutingResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class ConfiguratorTest {

  private final Configurator instance = Configurator.getInstance();
  private Resolver defaultResolver;

  @Before
  public void setup() {
    defaultResolver = Lookup.getDefaultResolver();
  }

  @After
  public void teardown() {
    Lookup.setDefaultResolver(defaultResolver);
  }

  @Test
  public void canGetInstance() {
    assertThat(instance, notNullValue());
  }

  @Test
  public void canRegister() {
    assertThat(defaultResolver, not(instanceOf(RoutingResolver.class)));

    instance.registerRoutingResolver();

    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));
  }

  @Test
  public void canUnRegister() {
    instance.registerRoutingResolver();
    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));

    instance.unRegisterRoutingResolver();
    assertThat(defaultResolver, not(instanceOf(RoutingResolver.class)));
  }
}