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

package com.openpojo.dns.config.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

import static com.openpojo.dns.config.utils.ServerAddressParser.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerAddressParserTest {

  @Test
  void shouldDefaultToPort53ForABareHost() throws UnknownHostException {
    assertThat(parse("10.0.0.1"), is(socket("10.0.0.1", 53)));
  }

  @Test
  void shouldReadAnExplicitPort() throws UnknownHostException {
    assertThat(parse("10.0.0.1:5353"), is(socket("10.0.0.1", 5353)));
  }

  @Test
  void shouldReadABracketedIpv6AddressWithPort() throws UnknownHostException {
    assertThat(parse("[::1]:5353"), is(socket("::1", 5353)));
  }

  @Test
  void shouldReadABracketedIpv6AddressWithoutPort() throws UnknownHostException {
    assertThat(parse("[::1]"), is(socket("::1", 53)));
  }

  @Test
  void shouldTreatABareIpv6AddressAsHavingNoPort() throws UnknownHostException {
    assertThat(parse("2001:db8::53"), is(socket("2001:db8::53", 53)));
  }

  @Test
  void shouldTrimSurroundingWhitespace() throws UnknownHostException {
    assertThat(parse(" 10.0.0.1:5353 "), is(socket("10.0.0.1", 5353)));
  }

  @Test
  void shouldRejectANonNumericPort() {
    UnknownHostException thrown = assertThrows(UnknownHostException.class, () -> parse("10.0.0.1:dns"));
    assertThat(thrown.getMessage(), containsString("Invalid port in [10.0.0.1:dns]"));
  }

  @Test
  void shouldRejectAPortOutOfRange() {
    UnknownHostException thrown = assertThrows(UnknownHostException.class, () -> parse("10.0.0.1:65536"));
    assertThat(thrown.getMessage(), containsString("Port out of range in [10.0.0.1:65536]"));
  }

  @Test
  void shouldRejectAPortOfZero() {
    assertThrows(UnknownHostException.class, () -> parse("[::1]:0"));
  }

  @Test
  void shouldRejectAnUnterminatedBracket() {
    UnknownHostException thrown = assertThrows(UnknownHostException.class, () -> parse("[::1:53"));
    assertThat(thrown.getMessage(), containsString("Unterminated IPv6 address [[::1:53]"));
  }

  @Test
  void shouldRejectAnUnknownHostName() {
    assertThrows(UnknownHostException.class, () -> parse("unknown.host.openpojo.com.:53"));
  }

  @Test
  void shouldFormatWhatItParses() throws UnknownHostException {
    assertThat(ServerAddressParser.format(parse("[::1]:5353")), is("[0:0:0:0:0:0:0:1]:5353"));
    assertThat(ServerAddressParser.format(parse("10.0.0.1")), is("10.0.0.1:53"));
  }

  @Test
  void shouldAcceptTheLowestAndHighestPorts() throws UnknownHostException {
    assertThat(parse("10.0.0.1:1").getPort(), is(1));
    assertThat(parse("10.0.0.1:65535").getPort(), is(65535));
  }

  @Test
  void shouldRejectAnEmptyHost() {
    UnknownHostException thrown = assertThrows(UnknownHostException.class, () -> parse(":53"));
    assertThat(thrown.getMessage(), containsString("Missing host in [:53]"));
  }

  private static InetSocketAddress socket(String address, int port) throws UnknownHostException {
    return new InetSocketAddress(InetAddress.getByName(address), port);
  }
}
