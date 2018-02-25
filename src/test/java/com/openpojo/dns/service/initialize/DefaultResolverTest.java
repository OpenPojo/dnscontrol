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

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import com.openpojo.dns.exception.ResolverException;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoField;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

import static com.openpojo.reflection.impl.PojoClassFactory.getPojoClass;
import static java.net.InetAddress.getByName;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.sameInstance;
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
  public void shouldSetupExtendedResolver() throws UnknownHostException {
    final String dnsAddress = "8.8.8.8";
    System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_NAMESERVERS, dnsAddress);
    defaultResolver.init();

    final Resolver defaultResolver = Lookup.getDefaultResolver();

    assertThat(defaultResolver, instanceOf(ExtendedResolver.class));
    PojoField resolversField = getField("resolvers", ExtendedResolver.class);
    List<Resolver> resolvers = (List<Resolver>) resolversField.get(defaultResolver);
    assertThat(resolvers.size(), is(1));

    Resolver configured = resolvers.get(0);
    assertThat(configured, instanceOf(SimpleResolver.class));

    PojoField addressField = getField("address", SimpleResolver.class);
    InetSocketAddress expected = new InetSocketAddress(getByName(dnsAddress), 53);
    assertThat((InetSocketAddress) addressField.get(configured), is(expected));
  }

  private PojoField getField(String name, Class<?> clazz) {
    PojoClass pojoClass = getPojoClass(clazz);
    for (PojoField field : pojoClass.getPojoFields())
      if (field.getName().equals(name)) return field;
    throw new RuntimeException("Field [" + name + "] not found in class [" + clazz.getName() + "]");
  }

  @Test
  public void shouldThrowResolverException() {
    final String unknownHost = "unknown.host.openpojo.com.";

    thrown.expect(ResolverException.class);
    thrown.expectMessage("Failed to setup default DNS Resolver using name servers [" + unknownHost + "]");
    thrown.expectCause(isA(UnknownHostException.class));

    System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_NAMESERVERS, unknownHost);
    defaultResolver.init();
  }
}
