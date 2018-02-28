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

import java.net.Inet4Address;
import java.net.UnknownHostException;

import com.openpojo.dns.routing.RoutingResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import static com.openpojo.dns.constants.TestConstants.SERVER_1_IPv4_STRING;
import static com.openpojo.dns.constants.TestConstants.SERVER_1_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class ConfiguratorTest {

  private final Configurator configurator = Configurator.getInstance();
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
    assertThat(configurator, notNullValue());
  }

  @Test
  public void canRegister() {
    assertThat(defaultResolver, not(instanceOf(RoutingResolver.class)));

    configurator.registerRoutingResolver();

    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));
  }

  @Test
  public void canUnRegister() {
    configurator.registerRoutingResolver();
    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));

    configurator.unRegisterRoutingResolver();
    assertThat(defaultResolver, not(instanceOf(RoutingResolver.class)));
  }

  @Test
  public void canResolveSomeEntry() throws TextParseException, UnknownHostException {
    configurator.registerRoutingResolver();
    final Name rootServer = new Name(SERVER_1_NAME);
    final String expectedIP = SERVER_1_IPv4_STRING;

    Record [] beforeConfigRecords = new Lookup(rootServer, Type.A).run(); // lookup IPAddress 4.
    assertThat(beforeConfigRecords.length, is(1));
    assertThat(((ARecord)beforeConfigRecords[0]).getAddress(), is(Inet4Address.getByName(expectedIP)));

    configurator.registerRoutingResolver();
    Record [] afterConfigRecords = new Lookup(rootServer, Type.A).run(); // lookup IPAddress 4.
    assertThat(beforeConfigRecords.length, is(1));
    assertThat(((ARecord)afterConfigRecords[0]).getAddress(), is(Inet4Address.getByName(expectedIP)));
  }
}