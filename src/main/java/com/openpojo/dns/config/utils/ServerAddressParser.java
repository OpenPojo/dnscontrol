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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Turns a DNS server entry from the configuration into a socket address. Accepted forms are {@code host},
 * {@code host:port}, {@code [ipv6]}, {@code [ipv6]:port} and a bare {@code ipv6} address. The port defaults to 53.
 */
public final class ServerAddressParser {
  public static final int DEFAULT_DNS_PORT = 53;

  public static InetSocketAddress parse(String entry) throws UnknownHostException {
    String value = entry.trim();
    String host = value;
    int port = DEFAULT_DNS_PORT;

    if (value.startsWith("[")) {
      int close = value.indexOf(']');
      if (close == -1)
        throw new UnknownHostException("Unterminated IPv6 address [" + entry + "]");
      host = value.substring(1, close);
      String rest = value.substring(close + 1);
      if (rest.startsWith(":"))
        port = parsePort(rest.substring(1), entry);
    } else if (value.indexOf(':') >= 0 && value.indexOf(':') == value.lastIndexOf(':')) {
      host = value.substring(0, value.indexOf(':'));
      port = parsePort(value.substring(value.indexOf(':') + 1), entry);
    }

    if (host.isEmpty())
      throw new UnknownHostException("Missing host in [" + entry + "]");
    return new InetSocketAddress(InetAddress.getByName(host), port);
  }

  /** The inverse of {@link #parse}: {@code host:port}, with IPv6 addresses in brackets. */
  public static String format(InetSocketAddress address) {
    String host = address.getAddress().getHostAddress();
    if (address.getAddress() instanceof Inet6Address)
      host = "[" + host + "]";
    return host + ":" + address.getPort();
  }

  private static int parsePort(String port, String entry) throws UnknownHostException {
    try {
      int value = Integer.parseInt(port);
      if (value < 1 || value > 0xFFFF)
        throw new UnknownHostException("Port out of range in [" + entry + "]");
      return value;
    } catch (NumberFormatException e) {
      throw new UnknownHostException("Invalid port in [" + entry + "]");
    }
  }

  private ServerAddressParser() { }
}
