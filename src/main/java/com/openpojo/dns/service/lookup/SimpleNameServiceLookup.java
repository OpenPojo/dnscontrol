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

import static com.openpojo.dns.service.lookup.ForwardLookupWithFallBack.getIPAddresses;

/**
 * @author oshoukry
 */
public class SimpleNameServiceLookup implements NameServiceLookup {
  private boolean IPv6Preference;

  public SimpleNameServiceLookup() {
    this(false);
  }

  public SimpleNameServiceLookup(boolean IPv6Preference) {
    this.IPv6Preference = IPv6Preference;
  }

  public boolean getIPv6Preference() {
    return IPv6Preference;
  }

  @Override
  public InetAddress[] lookupAllHostAddr(String hostName) throws UnknownHostException {
    final InetAddress[] ipAddresses = getIPAddresses(hostName, IPv6Preference);
    if (ipAddresses == null)
      throw new UnknownHostException("Unknown host [" + hostName + "]");
    return ipAddresses;
  }

  @Override
  public String getHostByAddr(byte[] ipAddress) throws UnknownHostException {
    String name = ReverseLookup.getHostName(ipAddress);
    if (name == null)
      throw new UnknownHostException("Unknown IPAddress " + Arrays.toString(ipAddress));
    return name;
  }
}
