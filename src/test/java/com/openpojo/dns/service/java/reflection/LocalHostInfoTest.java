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

import org.junit.Test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static com.openpojo.dns.constants.TestConstants.LOCAL_HOST;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LocalHostInfoTest {

  @Test
  public void canRetrieveLocalHostInfo() throws UnknownHostException {
    assertThat(LocalHostInfo.getHostName(), is(InetAddress.getLocalHost().getHostName()));
  }

  @Test
  public void canRetrieveHostIPAddresses() throws SocketException {
    assertThat(LocalHostInfo.getAllAddresses(), is(getInternetAddresses()));
  }

  @Test
  public void canRetrieveLocalHostName() {
    assertThat(LocalHostInfo.getLocalHostName(), equalToIgnoringCase(LOCAL_HOST));
  }

  @Test
  public void canRetrieveHostLoopBackAddresses() throws UnknownHostException {
    assertThat(Arrays.asList(LocalHostInfo.getLoopBackAddresses()), containsInAnyOrder(getLoopBackAddresses()));
  }

  private InetAddress[] getLoopBackAddresses() throws UnknownHostException {
    List<InetAddress> loopBackAddresses = new ArrayList<>();

    for (InetAddress address : InetAddress.getAllByName(LOCAL_HOST))
      if (address.isLoopbackAddress() && !address.isLinkLocalAddress())
        loopBackAddresses.add(InetAddress.getByAddress(address.getAddress()));

    return loopBackAddresses.toArray(new InetAddress[0]);
  }

  private InetAddress[] getInternetAddresses() throws SocketException {
    List<InetAddress> addresses = new ArrayList<>();
    // Java 1.8 returns all loop back interfaces only for localhost.
    Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
    while (networkInterfaceEnumeration.hasMoreElements()) {
      NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
      if (networkInterface.isUp()) {
        Enumeration<InetAddress> addressesEnumeration = networkInterface.getInetAddresses();

        while (addressesEnumeration.hasMoreElements()) {
          InetAddress inetAddress = addressesEnumeration.nextElement();
          if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
            addresses.add(inetAddress);
          }
        }
      }
    }
    return addresses.toArray(new InetAddress[0]);
  }
}
