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

package com.openpojo.dns.service;

import java.net.InetAddress;

import com.openpojo.dns.constants.TestConstants;
import com.openpojo.dns.service.initialize.DefaultDomain;
import com.openpojo.dns.service.initialize.DefaultIPv6Preference;
import com.openpojo.dns.service.initialize.DefaultResolver;
import com.openpojo.dns.service.initialize.Initializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class Dns4JavaNameServiceTest {

  private Dns4JavaNameService nameService;
  private DefaultDomainSpy defaultDomain;
  private DefaultIPv6PreferenceSpy ipV6Preference;
  private DefaultResolverSpy resolver;
  private String originalIPV6Preference;
  private final String hostname = TestConstants.SERVER_1_NAME;
  private final byte[] hostIP4AsBytes = TestConstants.SERVER_1_IPv4_BYTES;
  private final byte[] hostIP6AsBytes = TestConstants.SERVER_1_IPv6_BYTES;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    setPreferIPV6("" + false);
    defaultDomain = new DefaultDomainSpy();
    ipV6Preference = new DefaultIPv6PreferenceSpy();
    resolver = new DefaultResolverSpy();
    originalIPV6Preference = System.getProperty(Initializer.JAVA_NET_PREFER_IPV6_ADDRESSES);
  }

  @After
  public void teardown() {
    setPreferIPV6(originalIPV6Preference);
  }

  @Test
  public void shouldInitInitializers() {
    nameService = new Dns4JavaNameService(defaultDomain, ipV6Preference, resolver);
    assertThat(defaultDomain.initCalled, is(true));
    assertThat(ipV6Preference.initCalled, is(true));
    assertThat(resolver.initCalled, is(true));
  }

  @Test
  public void shouldSuccessfullyDoIP4ForwardLookup() {
    nameService = new Dns4JavaNameService(defaultDomain, ipV6Preference, resolver);
    final InetAddress[] inetAddresses = nameService.lookupAllHostAddr(hostname);
    assertThat(inetAddresses, notNullValue());
    assertThat(inetAddresses.length, is(1));
    assertThat(inetAddresses[0].getHostName(), is(hostname));
    assertThat(inetAddresses[0].getAddress(), is(hostIP4AsBytes));
  }


  @Test
  public void shouldSuccessfullyDoIP6ForwardLookup() {
    setPreferIPV6("" + true);
    nameService = new Dns4JavaNameService(defaultDomain, new DefaultIPv6Preference(), resolver);
    final InetAddress[] inetAddresses = nameService.lookupAllHostAddr(hostname);
    assertThat(inetAddresses, notNullValue());
    assertThat(inetAddresses.length, is(1));
    assertThat(inetAddresses[0].getHostName(), is(hostname));
    assertThat(inetAddresses[0].getAddress(), is(hostIP6AsBytes));
  }

  @Test
  public void shouldSuccessfullyDoIP4ReverseLookup() {
    nameService = new Dns4JavaNameService(defaultDomain, ipV6Preference, resolver);
    assertThat(nameService.getHostByAddr(hostIP4AsBytes), is(hostname));
  }

  @Test
  public void shouldSuccessfullyDoIP6ReverseLookup() {
    setPreferIPV6("" + true);
    nameService = new Dns4JavaNameService(defaultDomain, new DefaultIPv6Preference(), resolver);
    assertThat(nameService.getHostByAddr(hostIP6AsBytes), is(hostname));
  }

  private void setPreferIPV6(String preferIPV6) {
    if (preferIPV6 == null) {
      System.clearProperty(Initializer.JAVA_NET_PREFER_IPV6_ADDRESSES);
    } else {
      System.setProperty(Initializer.JAVA_NET_PREFER_IPV6_ADDRESSES, preferIPV6);
    }
  }

  private static class DefaultDomainSpy extends DefaultDomain {
    private boolean initCalled = false;

    @Override
    public void init() {
      initCalled = true;
    }
  }

  private static class DefaultIPv6PreferenceSpy extends DefaultIPv6Preference {
    private boolean initCalled = false;

    @Override
    public void init() {
      initCalled = true;
    }
  }

  private static class DefaultResolverSpy extends DefaultResolver {
    private boolean initCalled = false;

    @Override
    public void init() {
      initCalled = true;
    }
  }
}