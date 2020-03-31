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

package com.openpojo.dns.service.lookup;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.openpojo.dns.exception.ResolveException;
import com.openpojo.dns.service.lookup.impl.HostsFileNameService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.Lookup;

import static com.openpojo.dns.constants.TestConstants.*;
import static com.openpojo.dns.routing.RoutingTable.DOT;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class SimpleNameServiceLookupTest {

  @Before
  @After
  public void setupAndTeardown() {
    System.clearProperty(HostsFileNameService.JDK_NET_HOSTS_FILE);
    Lookup.refreshDefault();
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldBeAbleToConstructWithPreference() {
    assertThat(createNameServiceLookup(false).getIPv6Preference(), is(false));
    assertThat(createNameServiceLookup(true).getIPv6Preference(), is(true));
  }

  @Test
  public void shouldBeAbleToLookupIPv4() throws UnknownHostException {
    final InetAddress[] inetAddresses = createNameServiceLookup(false).lookupAllHostAddr(SERVER_1_NAME);

    assertThat(inetAddresses, notNullValue());
    assertThat(inetAddresses.length, is(1));
    assertThat(inetAddresses[0].getAddress(), is(SERVER_1_IPv4_BYTES));
  }

  @Test
  public void shouldBeAbleToLookupIPv6() throws UnknownHostException {
    final InetAddress[] inetAddresses = createNameServiceLookup(true).lookupAllHostAddr(SERVER_1_NAME);

    assertThat(inetAddresses, notNullValue());
    assertThat(inetAddresses.length, is(1));
    assertThat(inetAddresses[0].getAddress(), is(SERVER_1_IPv6_BYTES));
  }

  @Test
  public void shouldFailIPv6LookupAndFallBackToIPv4() throws UnknownHostException {
    final InetAddress[] inetAddresses = createNameServiceLookup(true).lookupAllHostAddr(SERVER_2_NAME);

    assertThat(inetAddresses, notNullValue());
    assertThat(inetAddresses.length, is(1));

    assertThat(inetAddresses[0].getAddress(), is(SERVER_2_IPv4_BYTES));
  }

  @Test
  public void shouldThrowExceptionForUnknownHost() throws UnknownHostException {
    thrown.expect(UnknownHostException.class);
    thrown.expectMessage("Unknown host [" + UNKNOWN_SERVER + "]");

    createNameServiceLookup(false).lookupAllHostAddr(UNKNOWN_SERVER);
  }

  @Test
  public void shouldThrowExceptionForInvalidHostName() throws UnknownHostException {
    thrown.expect(ResolveException.class);
    thrown.expectMessage("Failed to parse name []");

    createNameServiceLookup(false).lookupAllHostAddr("");
  }

  @Test
  public void shouldThrowExceptionForNullHostName() throws UnknownHostException {
    thrown.expect(ResolveException.class);
    thrown.expectMessage("Failed to parse name [null]");

    createNameServiceLookup(false).lookupAllHostAddr(null);
  }

  @Test
  public void shouldThrowExceptionForNullReverseLookup() throws UnknownHostException {
    thrown.expect(ResolveException.class);
    thrown.expectMessage("Failed to parse address [null]");
    thrown.expectCause(allOf(
        isA(UnknownHostException.class),
        hasProperty("message", is("addr is of illegal length"))));

    createNameServiceLookup(false).getHostByAddr(null);
  }

  @Test
  public void shouldThrowExceptionForInvalidReverseLookup() throws UnknownHostException {
    final byte[] addr = { 0 };

    thrown.expect(ResolveException.class);
    thrown.expectMessage("Failed to parse address [[0]]");
    thrown.expectCause(allOf(
        isA(UnknownHostException.class),
        hasProperty("message", is("addr is of illegal length"))));

    createNameServiceLookup(false).getHostByAddr(addr);
  }

  @Test
  public void shouldThrowExceptionForUnknownIPAddressBytes() throws UnknownHostException {
    final byte[] addr = { 0, 1, 2, 3 };
    thrown.expect(UnknownHostException.class);
    thrown.expectMessage("Unknown IPAddress " + Arrays.toString(addr));

    createNameServiceLookup(false).getHostByAddr(addr);
  }

  @Test
  public void shouldGetIPAddressForLocalhost() throws UnknownHostException {
    final InetAddress[] localhosts = createNameServiceLookup(false).lookupAllHostAddr(LOCAL_HOST);
    assertThat(localhosts, notNullValue());
    assertThat(localhosts.length, greaterThan(0));
    for (InetAddress localhost : localhosts) {
      assertThat(localhost.isLoopbackAddress(), is(true));
    }
  }

  @Test
  public void shouldGetNameForReverseLookupLoopBackAddressIP4() throws UnknownHostException {
    String localhost = createNameServiceLookup(false).getHostByAddr(LOCAL_HOST_IPv4_BYTES);
    assertThat(localhost, isOneOf(LOCAL_HOST, LOCAL_HOST + DOT));
  }

  @Test
  @Ignore("Failing on travis, disabling for now.")
  public void shouldGetNameForReverseLookupLoopBackAddressIP6() throws UnknownHostException {
    String localhost = createNameServiceLookup(false).getHostByAddr(LOCAL_HOST_IPv6_BYTES);
    assertThat(localhost, isOneOf(LOCAL_HOST, LOCAL_HOST + DOT));
  }

  @Test
  public void shouldGetIPAddressForHostByName() throws UnknownHostException {
    final InetAddress[] localIPAddresses = createNameServiceLookup(false).lookupAllHostAddr(InetAddress.getLocalHost().getHostName());
    assertThat(localIPAddresses, notNullValue());
    assertThat(localIPAddresses.length, greaterThan(0));
    for (InetAddress localhost : localIPAddresses) {
      assertThat(!localhost.isLoopbackAddress(), is(true));
    }
  }

  @Test
  public void shouldUtilizeHostsFile() throws UnknownHostException {
    System.setProperty(HostsFileNameService.JDK_NET_HOSTS_FILE, "hosts.test.file");
    final SimpleNameServiceLookup nameServiceLookup = createNameServiceLookup(false);
    final InetAddress[] actual = nameServiceLookup.lookupAllHostAddr(HOSTS_SERVER_1_NAME);
    assertThat(actual, notNullValue());
    assertThat(actual, is(InetAddress.getAllByName(HOSTS_SERVER_1_IPv4_String)));
    assertThat(nameServiceLookup.getHostByAddr(HOSTS_SERVER_1_IPv4_BYTES), is(HOSTS_SERVER_1_NAME));
  }

  private SimpleNameServiceLookup createNameServiceLookup(boolean iPv6Preference) {
    return new SimpleNameServiceLookup(iPv6Preference);
  }
}
