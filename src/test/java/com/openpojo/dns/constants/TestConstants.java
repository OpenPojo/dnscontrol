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

package com.openpojo.dns.constants;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The Entries in this file are for real servers, but they are also subject to change creating fragility in the tests.
 * Putting them all together in one location helps update many tests that use those in one location.
 *
 * @author oshoukry
 */
public class TestConstants {
  public static final String UNKNOWN_SERVER = "unknown.host.openpojo.com.";

  public static final String SERVER_1_DOMAIN = "root-servers.net.";
  public static final String SERVER_1_NAME = "a." + SERVER_1_DOMAIN;

  public static final String SERVER_1_IPv4_STRING = "198.41.0.4";
  public static final byte[] SERVER_1_IPv4_BYTES = getAddressBytes(SERVER_1_IPv4_STRING);

  public static final String SERVER_1_IPv6_STRING = "2001:503:ba3e::2:30";
  public static final byte[] SERVER_1_IPv6_BYTES = getAddressBytes(SERVER_1_IPv6_STRING);

  public static final String SERVER_2_NAME = "egypt.gov.eg.";
  public static final String SERVER_2_IPv4_STRING = "81.21.103.81";
  public static final byte[] SERVER_2_IPv4_BYTES = getAddressBytes(SERVER_2_IPv4_STRING);

  public static final String LOCAL_HOST = "localhost";
  public static final String LOCAL_HOST_IPv4 = "127.0.0.1";
  public static final byte[] LOCAL_HOST_IPv4_BYTES = getAddressBytes(LOCAL_HOST_IPv4);
  public static final String LOCAL_HOST_IPv6 = "::1";
  public static final byte[] LOCAL_HOST_IPv6_BYTES = getAddressBytes(LOCAL_HOST_IPv6);

  public static final String DNS_CONTROL_CONFIG = "dnscontrol.test.conf";

  /**
   * The Entries that are in the hosts.test.file on the classpath.
   */
  // 10.0.0.5 server5 server50
  public static final String HOSTS_SERVER_50_NAME = "server50";
  public static final String HOSTS_SERVER_50_IPv4_STRING = "10.0.0.5";
  public static final byte[] HOSTS_SERVER_50_IPv4_BYTES = getAddressBytes(HOSTS_SERVER_50_IPv4_STRING);
  public static final String HOSTS_SERVER_50_REVERSE_LOOKUP_NAME = "server5.com";
  // 10.0.0.1 mylocalserver
  public static final String HOSTS_SERVER_1_NAME = "mylocalserver";
  public static final String HOSTS_SERVER_1_IPv4_String = "10.0.0.1";
  public static final byte[] HOSTS_SERVER_1_IPv4_BYTES = getAddressBytes(HOSTS_SERVER_1_IPv4_String);

  private static byte[] getAddressBytes(String ip_asString) {
    try {
      return InetAddress.getByName(ip_asString).getAddress();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
}
