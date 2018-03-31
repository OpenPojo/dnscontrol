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

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.openpojo.dns.exception.ResolveException;
import com.openpojo.random.RandomFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.openpojo.dns.constants.TestConstants.HOSTS_SERVER_50_IPv4_BYTES;
import static com.openpojo.dns.constants.TestConstants.HOSTS_SERVER_50_IPv4_STRING;
import static com.openpojo.dns.constants.TestConstants.HOSTS_SERVER_50_NAME;
import static com.openpojo.dns.constants.TestConstants.HOSTS_SERVER_50_REVERSE_LOOKUP_NAME;
import static com.openpojo.dns.service.lookup.util.NameFactory.getName;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class HostsFileNameServiceTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  @After
  public void setupAndTeardown() {
    System.clearProperty(HostsFileNameService.JDK_NET_HOSTS_FILE);
  }

  @Test
  public void canReadEnvironmentVariable() {
    assertThat(new HostsFileNameService().getJdkNetHostsFile(), nullValue());
    final String anyString = RandomFactory.getRandomValue(String.class);
    System.setProperty(HostsFileNameService.JDK_NET_HOSTS_FILE, anyString);
    assertThat(new HostsFileNameService().getJdkNetHostsFile(), is(anyString));
  }

  @Test
  public void canReadConfigurationFileAndOnlyFirstHostConfiguredIsReturnedOnReverseLookup() throws UnknownHostException {
    System.setProperty(HostsFileNameService.JDK_NET_HOSTS_FILE, "hosts.test.file");
    final HostsFileNameService hostsFileNameService = new HostsFileNameService();
    final InetAddress[] addresses = hostsFileNameService.lookupAllHostAddr(getName(HOSTS_SERVER_50_NAME));
    assertThat(addresses, is(InetAddress.getAllByName(HOSTS_SERVER_50_IPv4_STRING)));
    assertThat(hostsFileNameService.getHostByAddr(getName(HOSTS_SERVER_50_IPv4_BYTES)), is(HOSTS_SERVER_50_REVERSE_LOOKUP_NAME));
  }

  @Test
  public void shouldFailToParseHostFile() {
    thrown.expect(ResolveException.class);
    thrown.expectMessage("Failed to parse address [fe80:0:0:0:0:0:0:0:1%unknown0]");

    System.setProperty(HostsFileNameService.JDK_NET_HOSTS_FILE, "./src/test/files/hosts.another");
    new HostsFileNameService();
  }
}
