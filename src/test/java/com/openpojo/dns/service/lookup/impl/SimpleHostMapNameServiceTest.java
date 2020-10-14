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

package com.openpojo.dns.service.lookup.impl;

import com.openpojo.random.RandomFactory;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.TextParseException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author oshoukry
 */
public class SimpleHostMapNameServiceTest {

  private SimpleHostMapNameService nameService;

  @Before
  public void setup() {
    nameService = new SimpleHostMapNameService();
  }

  @Test
  public void canResolveHostNameToIP() throws UnknownHostException, TextParseException {
    final InetAddress localHost = InetAddress.getLocalHost();
    final InetAddress[] inetAddresses = nameService.lookupAllHostAddr(Name.fromString(localHost.getHostName().toUpperCase()));
    assertThat(inetAddresses, notNullValue());
    assertThat(inetAddresses.length, greaterThan(0));
  }

  @Test
  public void canReverseLookupIPToHostName() throws UnknownHostException {
    final InetAddress localHost = InetAddress.getLocalHost();
    byte[] address = localHost.getAddress();

    Name name = ReverseMap.fromAddress(address);
    final String hostByAddr = nameService.getHostByAddr(name);
    assertThat("\nGiven: \n\t" + localHost + ",\n\t" + nameService, hostByAddr,
        anyOf(is(localHost.getHostName() /* JDK 1.8+ returns machine name */),
            is("localhost" /* for JDK 1.7 */)));
  }

  @Test
  public void shouldReturnNullWhenUnrecognizedHostname() throws TextParseException {
    final String randomValue = RandomFactory.getRandomValue(String.class);
    assertThat(nameService.getHostByAddr(Name.fromString(randomValue)), nullValue());
  }

  @Test
  public void shouldReturnNullWhenUnrecognizedByteAddress() {
    byte[] randomValue = new byte[4];
    for (int i = 0; i < randomValue.length; i++)
      randomValue[i] = RandomFactory.getRandomValue(byte.class);
    assertThat(nameService.getHostByAddr(ReverseMap.fromAddress(randomValue)), nullValue());

    randomValue = new byte[16];
    for (int i = 0; i < randomValue.length; i++)
      randomValue[i] = RandomFactory.getRandomValue(byte.class);
    assertThat(nameService.getHostByAddr(ReverseMap.fromAddress(randomValue)), nullValue());
  }
}
