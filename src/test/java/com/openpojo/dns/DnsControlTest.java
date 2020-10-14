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

import com.openpojo.dns.cache.CacheControl;
import com.openpojo.dns.cache.utils.VerificationHelper;
import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.config.impl.EnvironmentDnsConfigReader;
import com.openpojo.dns.exception.RouteSetupException;
import com.openpojo.dns.resolve.RoutingResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.*;

import static com.openpojo.dns.DnsControl.recreateInstance;
import static com.openpojo.dns.constants.TestConstants.SERVER_1_IPv4_STRING;
import static com.openpojo.dns.constants.TestConstants.SERVER_1_NAME;
import static com.openpojo.dns.constants.TestConstants.UNKNOWN_SERVER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author oshoukry
 */
public class DnsControlTest {

  private final DnsControl dnsControl = DnsControl.getInstance();
  private Resolver defaultResolver;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    System.clearProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY);
    Lookup.refreshDefault();
    CacheControl.resetCache();
    defaultResolver = Lookup.getDefaultResolver();
  }

  @After
  public void teardown() {
    System.clearProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY);
    Lookup.refreshDefault();
    CacheControl.resetCache();
  }

  @Test
  public void canGetInstance() {
    assertThat(dnsControl, notNullValue());
  }

  @Test
  public void canRegister() {
    assertThat(defaultResolver, not(instanceOf(RoutingResolver.class)));
    assertThat(dnsControl.isRoutingResolverRegistered(), is(false));

    dnsControl.registerRoutingResolver();
    assertThat(dnsControl.isRoutingResolverRegistered(), is(true));

    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));
  }

  @Test
  public void canUnRegister() {
    dnsControl.registerRoutingResolver();
    assertThat(dnsControl.isRoutingResolverRegistered(), is(true));
    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));

    dnsControl.unRegisterRoutingResolver();
    assertThat(defaultResolver, not(instanceOf(RoutingResolver.class)));
    assertThat(dnsControl.isRoutingResolverRegistered(), is(false));
  }

  @Test
  public void canResolveSomeEntry() throws TextParseException, UnknownHostException {
    dnsControl.registerRoutingResolver();
    final Name rootServer = new Name(SERVER_1_NAME);
    final String expectedIP = SERVER_1_IPv4_STRING;

    Record[] beforeConfigRecords = new Lookup(rootServer, Type.A).run(); // lookup IPAddress 4.
    assertThat(beforeConfigRecords.length, is(1));
    assertThat(((ARecord) beforeConfigRecords[0]).getAddress(), is(Inet4Address.getByName(expectedIP)));

    dnsControl.registerRoutingResolver();
    Record[] afterConfigRecords = new Lookup(rootServer, Type.A).run(); // lookup IPAddress 4.
    assertThat(beforeConfigRecords.length, is(1));
    assertThat(((ARecord) afterConfigRecords[0]).getAddress(), is(Inet4Address.getByName(expectedIP)));
  }

  @Test
  public void whenChangingRoutingTableAutoRegisterRoutingResolver() {
    assertThat(dnsControl.isRoutingResolverRegistered(), is(false));
    assertThat(Lookup.getDefaultResolver(), not(instanceOf(RoutingResolver.class)));
    dnsControl.setRoutingTable(null);
    assertThat(dnsControl.isRoutingResolverRegistered(), is(true));
    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));
  }

  @Test
  public void shouldClearCacheWheneverRoutingTableChanges() throws TextParseException {
    dnsControl.registerRoutingResolver();
    VerificationHelper.verifyCacheIsEmpty();

    new Lookup(SERVER_1_NAME, DClass.IN).run();
    assertThat(Lookup.getDefaultCache(DClass.IN).getSize(), is(1));
    dnsControl.setRoutingTable(null);
    VerificationHelper.verifyCacheIsEmpty();
  }

  @Test
  public void shouldThrowResolverException() {
    thrown.expect(RouteSetupException.class);
    thrown.expectMessage(
        "Unknown host defined in configuration reader [" + EnvironmentDnsConfigReader.class.getName() + "] " + UNKNOWN_SERVER);

    thrown.expectCause(isA(java.net.UnknownHostException.class));

    System.setProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY, UNKNOWN_SERVER);
    recreateInstance();
  }

  @Test
  public void whenRecreateGetInstanceShouldBeDifferent() {
    DnsControl instance = DnsControl.getInstance();
    assertThat(instance, sameInstance(DnsControl.getInstance()));
    DnsControl differentInstance = DnsControl.recreateInstance();
    assertThat(differentInstance, not(sameInstance(instance)));
    assertThat(differentInstance, sameInstance(DnsControl.getInstance()));
  }
}
