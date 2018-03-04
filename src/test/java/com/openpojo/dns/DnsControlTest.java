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

import com.openpojo.dns.cache.utils.VerificationHelper;
import com.openpojo.dns.routing.RoutingResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.*;

import static com.openpojo.dns.constants.TestConstants.SERVER_1_IPv4_STRING;
import static com.openpojo.dns.constants.TestConstants.SERVER_1_NAME;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class DnsControlTest {

  private final DnsControl dnsControl = DnsControl.getInstance();
  private Resolver defaultResolver;

  @Before
  public void setup() {
    Lookup.refreshDefault();
    defaultResolver = Lookup.getDefaultResolver();
  }

  @After
  public void teardown() {
    Lookup.refreshDefault();
  }

  @Test
  public void canGetInstance() {
    assertThat(dnsControl, notNullValue());
  }

  @Test
  public void canRegister() {
    assertThat(defaultResolver, not(instanceOf(RoutingResolver.class)));

    dnsControl.registerRoutingResolver();

    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));
  }

  @Test
  public void canUnRegister() {
    dnsControl.registerRoutingResolver();
    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));

    dnsControl.unRegisterRoutingResolver();
    assertThat(defaultResolver, not(instanceOf(RoutingResolver.class)));
  }

  @Test
  public void canResolveSomeEntry() throws TextParseException, UnknownHostException {
    dnsControl.registerRoutingResolver();
    final Name rootServer = new Name(SERVER_1_NAME);
    final String expectedIP = SERVER_1_IPv4_STRING;

    Record [] beforeConfigRecords = new Lookup(rootServer, Type.A).run(); // lookup IPAddress 4.
    assertThat(beforeConfigRecords.length, is(1));
    assertThat(((ARecord)beforeConfigRecords[0]).getAddress(), is(Inet4Address.getByName(expectedIP)));

    dnsControl.registerRoutingResolver();
    Record [] afterConfigRecords = new Lookup(rootServer, Type.A).run(); // lookup IPAddress 4.
    assertThat(beforeConfigRecords.length, is(1));
    assertThat(((ARecord)afterConfigRecords[0]).getAddress(), is(Inet4Address.getByName(expectedIP)));
  }

  @Test
  public void shouldClearCacheWhenverRoutingTableChanges() throws TextParseException {
    dnsControl.registerRoutingResolver();
    VerificationHelper.verifyCacheIsEmpty();

    new Lookup(SERVER_1_NAME, DClass.IN).run();
    assertThat(Lookup.getDefaultCache(DClass.IN).getSize(), is(1));
    dnsControl.setRoutingTable(null);
    VerificationHelper.verifyCacheIsEmpty();
  }
}
