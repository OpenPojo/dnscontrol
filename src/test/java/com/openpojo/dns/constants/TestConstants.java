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


  public static final String SERVER_1_NAME = "a.root-servers.net.";

  public static final String SERVER_1_IPv4_STRING = "198.41.0.4";
  public static final byte[] SERVER_1_IPv4_BYTES = getAddressBytes(SERVER_1_IPv4_STRING);

  public static final String SERVER_1_IPv6_STRING = "2001:503:ba3e::2:30";
  public static final byte[] SERVER_1_IPv6_BYTES = getAddressBytes(SERVER_1_IPv6_STRING);

  public static final String SERVER_2_NAME = "slashdot.com.";
  public static final String SERVER_2_IPv4_STRING = "216.105.38.15";
  public static final byte[] SERVER_2_IPv4_BYTES = getAddressBytes(SERVER_2_IPv4_STRING);

  private static byte[] getAddressBytes(String ip_asString) {
    try {
      return InetAddress.getByName(ip_asString).getAddress();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

}
