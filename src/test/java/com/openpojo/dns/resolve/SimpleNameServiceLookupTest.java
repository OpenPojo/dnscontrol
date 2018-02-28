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

package com.openpojo.dns.resolve;

import java.net.InetAddress;
import java.util.Arrays;

import com.openpojo.dns.exception.ResolveException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.openpojo.dns.constants.TestConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class SimpleNameServiceLookupTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void initialIPv6PreferenceShouldBeFalse() {
    assertThat(new SimpleNameServiceLookup().getIPv6Preference(), is(false));
  }

  @Test
  public void shouldBeAbleToConstructWithPreference() {
    assertThat(createNameServiceLookup(false).getIPv6Preference(), is(false));
    assertThat(createNameServiceLookup(true).getIPv6Preference(), is(true));
  }

  @Test
  public void shouldBeAbleToLookupIPv4() {
    final InetAddress[] inetAddresses = createNameServiceLookup(false).lookupAllHostAddr(SERVER_1_NAME);

    assertThat(inetAddresses, notNullValue());
    assertThat(inetAddresses.length, is(1));
    assertThat(inetAddresses[0].getAddress(), is(SERVER_1_IPv4_BYTES));
  }

  @Test
  public void shouldBeAbleToLookupIPv6() {
    final InetAddress[] inetAddresses = createNameServiceLookup(true).lookupAllHostAddr(SERVER_1_NAME);

    assertThat(inetAddresses, notNullValue());
    assertThat(inetAddresses.length, is(1));
    assertThat(inetAddresses[0].getAddress(), is(SERVER_1_IPv6_BYTES));
  }

  @Test
  public void shouldFailIPv6LookupAndFallBackToIPv4() {
    final InetAddress[] inetAddresses = createNameServiceLookup(true).lookupAllHostAddr(SERVER_2_NAME);

    assertThat(inetAddresses, notNullValue());
    assertThat(inetAddresses.length, is(1));
    assertThat(inetAddresses[0].getAddress(), is(SERVER_2_IPv4_BYTES));

  }

  @Test
  public void shouldThrowExceptionForUnknownHost() {
    thrown.expect(ResolveException.class);
    thrown.expectMessage("Unknown host [" + UNKNOWN_SERVER + "]");

    createNameServiceLookup(false).lookupAllHostAddr(UNKNOWN_SERVER);
  }

  @Test
  public void shouldThrowExceptionForInvalidHostName() {
    thrown.expect(ResolveException.class);
    thrown.expectMessage("Failed to parse name []");

    createNameServiceLookup(false).lookupAllHostAddr("");
  }

  @Test
  public void shouldThrowExceptionForNullHostName() {
    thrown.expect(ResolveException.class);
    thrown.expectMessage("Failed to parse name [null]");

    createNameServiceLookup(false).lookupAllHostAddr(null);
  }

  @Test
  public void shouldThrowExceptionForNullReverseLookup() {
    thrown.expect(ResolveException.class);
    thrown.expectMessage("Unknown host null");

    createNameServiceLookup(false).getHostByAddr(null);
  }

  @Test
  public void shouldThrowExceptionForInvalidReverseLookup() {
    final byte[] addr = { 0 };
    thrown.expect(ResolveException.class);
    thrown.expectMessage("Unknown host " + Arrays.toString(addr));

    createNameServiceLookup(false).getHostByAddr(addr);
  }

  @Test
  public void shouldThrowExceptionForUnknownIPAddressBytes() {
    final byte[] addr = { 0, 1, 2, 3 };
    thrown.expect(ResolveException.class);
    thrown.expectMessage("Unknown host " + Arrays.toString(addr));

    createNameServiceLookup(false).getHostByAddr(addr);
  }

  private SimpleNameServiceLookup createNameServiceLookup(boolean iPv6Preference) {
    return new SimpleNameServiceLookup(iPv6Preference);
  }
}