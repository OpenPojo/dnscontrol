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

package com.openpojo.dns.service.initialize;

import java.net.UnknownHostException;

import com.openpojo.dns.exception.RouteSetupException;
import com.openpojo.dns.routing.RoutingResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;

import static com.openpojo.dns.constants.TestConstants.UNKNOWN_SERVER;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class DefaultResolverTest {
  private Resolver preTestResolver;
  private DefaultResolver defaultResolver;

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private String preTestNameServers;

  @Before
  public void setup() {
    preTestResolver = Lookup.getDefaultResolver();
    preTestNameServers = System.getProperty(Initializer.SUN_NET_SPI_NAMESERVICE_NAMESERVERS);
    System.clearProperty(Initializer.SUN_NET_SPI_NAMESERVICE_NAMESERVERS);

    defaultResolver = new DefaultResolver();
  }

  @After
  public void teardown() {
    Lookup.setDefaultResolver(preTestResolver);
    if (preTestNameServers != null) System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_NAMESERVERS, preTestNameServers);
    else System.clearProperty(Initializer.SUN_NET_SPI_NAMESERVICE_NAMESERVERS);
  }

  @Test
  public void shouldNotChangeDefaultResolver() {
    defaultResolver.init();
    assertThat(Lookup.getDefaultResolver(), sameInstance(preTestResolver));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldSetupRoutingResolver() throws UnknownHostException {
    final String dnsAddress = "8.8.8.8";
    System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_NAMESERVERS, dnsAddress);
    defaultResolver.init();

    final Resolver defaultResolver = Lookup.getDefaultResolver();

    assertThat(defaultResolver, instanceOf(RoutingResolver.class));
  }

  @Test
  public void shouldThrowResolverException() {

    thrown.expect(RouteSetupException.class);
    thrown.expectMessage("Failed to create dns routing map");

    thrown.expectCause(isA(UnknownHostException.class));

    System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_NAMESERVERS, UNKNOWN_SERVER);
    defaultResolver.init();
  }
}
