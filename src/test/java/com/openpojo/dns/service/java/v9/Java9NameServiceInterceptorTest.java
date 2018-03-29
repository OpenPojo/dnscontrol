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

package com.openpojo.dns.service.java.v9;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openpojo.dns.DnsControl;
import com.openpojo.dns.service.lookup.SimpleNameServiceLookup;
import com.openpojo.reflection.java.load.ClassUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.Lookup;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

/**
 * @author oshoukry
 */
public class Java9NameServiceInterceptorTest {

  private Java9NameServiceInterceptor java9NameServiceInterceptor;
  private static Field nameServiceField;
  private static Object nameServiceFieldValue;
  private static byte[] ipv4Address;
  private static byte[] ipv6Address;
  private static byte[] specialLoopBackAddress;
  private static String localhost = "localhost.";
  private static String ipv4LoopBack = "127.0.0.1";
  private static AtomicBoolean testCanRun = new AtomicBoolean(false);

  @Before
  public void setup() {
    try {
      ipv4Address = InetAddress.getByName(ipv4LoopBack).getAddress();
      ipv6Address = InetAddress.getByName("::1").getAddress();
      specialLoopBackAddress = InetAddress.getByName("fe80::1").getAddress();
      assumeThat(ClassUtil.loadClass("java.net.InetAddress$NameService"), notNullValue());

      //noinspection JavaReflectionMemberAccess
      nameServiceField = InetAddress.class.getDeclaredField("nameService");
      nameServiceField.setAccessible(true);
      nameServiceFieldValue = nameServiceField.get(null);

      Lookup.refreshDefault();
      DnsControl.recreateInstance().registerRoutingResolver();

      java9NameServiceInterceptor = new Java9NameServiceInterceptor();
      java9NameServiceInterceptor.changeInetAddressProxy(java9NameServiceInterceptor.createProxyForNameService());
      testCanRun = new AtomicBoolean(true);
    } catch (Exception ignored) {
      assumeThat("Ignoring running tests as setup failed!", false, is(true));
    }
  }

  @After
  public void teardown() throws IllegalAccessException {
    if (testCanRun.get()) {
      if (nameServiceField != null)
        nameServiceField.set(null, nameServiceFieldValue);
      DnsControl.getInstance().unRegisterRoutingResolver();
      Lookup.refreshDefault();
    }
  }

  @Test
  public void forwardResolution() throws UnknownHostException {
    final InetAddress[] loopBackAddresses = InetAddress.getAllByName(localhost);

    assertThat(loopBackAddresses.length, greaterThan(0));
    for (InetAddress address : loopBackAddresses) {
      assertThat(address.getHostName(), is(localhost));
      assertThat(address.getAddress(), anyOf(is(ipv4Address), is(ipv6Address), is(specialLoopBackAddress)));
    }
  }

  @Test
  public void reverseResolution() throws UnknownHostException {
    final InetAddress byAddress = InetAddress.getByAddress(ipv4Address);
    assertThat(byAddress.getHostName(), is(localhost));
  }

  @Test
  public void proxyServiceShouldHandleToString() throws ClassNotFoundException {
    final Object proxyForNameService = java9NameServiceInterceptor.createProxyForNameService();
    assertThat(proxyForNameService.toString(), startsWith(SimpleNameServiceLookup.class.getName() + ".Generated_Proxy"));
  }

  @Test
  public void proxyServiceShouldNotThrowExceptionOnEquals() throws ClassNotFoundException {
    final Object proxyForNameService = java9NameServiceInterceptor.createProxyForNameService();
    assertThat(proxyForNameService.equals(new Object()), is(false));
  }

  @Test
  public void proxyServiceShouldNotThrowExceptionOnHashCode() throws ClassNotFoundException {
    final Object proxyForNameService = java9NameServiceInterceptor.createProxyForNameService();
    assertThat(proxyForNameService.hashCode(), notNullValue());
  }
}
