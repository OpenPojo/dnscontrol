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

package com.openpojo.dns.playpen;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author oshoukry
 */
@Ignore
public class NetworkInterfacesTest {
  @Test
  public void printAllHostNamesOnNetworkInterfaces() throws SocketException, UnknownHostException {
    final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
    while (networkInterfaces.hasMoreElements()) {
      NetworkInterface networkInterface = networkInterfaces.nextElement();
      System.out.println("Display Name: " + networkInterface.getDisplayName());
      System.out.println("Name: " + networkInterface.getName());
      final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
      while (inetAddresses.hasMoreElements()) {
        final InetAddress inetAddress = inetAddresses.nextElement();
        System.out.println("\t Hostname: " + inetAddress.getHostName() + ", IP: " + inetAddress.getHostAddress());
      }
    }
    System.out.println(InetAddress.getLocalHost().getHostName());
    System.out.println(InetAddress.getLocalHost().getHostAddress());
  }
}
