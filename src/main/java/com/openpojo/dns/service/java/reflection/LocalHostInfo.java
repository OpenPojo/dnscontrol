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

package com.openpojo.dns.service.java.reflection;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.openpojo.dns.service.java.reflection.ReflectionHelper.getFieldValue;
import static com.openpojo.dns.service.java.reflection.ReflectionHelper.invokeMethodOnClass;

public class LocalHostInfo {
  private static final String LOCAL_HOST = "localhost";

  public static String getHostName() {
    return (String) invokeMethodOnClass(null, "getLocalHostName", getInetAddressImpl());
  }

  public static String getLocalHostName() {
    return LOCAL_HOST;
  }

  public static InetAddress[] getLoopBackAddresses() {
    return getAllAddressesOnUpInterfaces(true);
  }

  public static InetAddress[] getAllAddresses() {
    return getAllAddressesOnUpInterfaces(false);
  }

  private static InetAddress[] getAllAddressesOnUpInterfaces(boolean loopBackOnly) {
    List<InetAddress> addresses = new ArrayList<>();
    for (NetworkInterface networkInterface : getAllUpInterfaces())
      for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
        final InetAddress address = interfaceAddress.getAddress();
        if (!address.isLinkLocalAddress() && (loopBackOnly == address.isLoopbackAddress())) {
          addresses.add(address);
        }
      }
    return addresses.toArray(new InetAddress[0]);
  }

  private static List<NetworkInterface> getAllUpInterfaces() {
    List<NetworkInterface> allUpInterfaces = new ArrayList<>();
    try {
      final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface netInterface = networkInterfaces.nextElement();
        try {
          if (netInterface.isUp()) {
            allUpInterfaces.add(netInterface);
          }
        } catch (SocketException ignored) {
          // Continue even if one interface throws an error.
        }
      }
    } catch (SocketException ignored) { }
    return allUpInterfaces;
  }

  private static Object getInetAddressImpl() {
    return getFieldValue(InetAddress.class, "impl", null);
  }

  private LocalHostInfo() {
  }
}
