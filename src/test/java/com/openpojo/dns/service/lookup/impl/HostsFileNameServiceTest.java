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

import com.openpojo.dns.exception.ResolveException;
import com.openpojo.dns.service.lookup.util.NameFactory;
import com.openpojo.random.RandomFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.Name;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.openpojo.dns.constants.TestConstants.HOSTS_SERVER_50_IPv4_BYTES;
import static com.openpojo.dns.constants.TestConstants.HOSTS_SERVER_50_IPv4_STRING;
import static com.openpojo.dns.constants.TestConstants.HOSTS_SERVER_50_NAME;
import static com.openpojo.dns.constants.TestConstants.HOSTS_SERVER_50_REVERSE_LOOKUP_NAME;
import static com.openpojo.dns.service.lookup.util.NameFactory.getName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

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
    final String someValue = anyString();
    System.setProperty(HostsFileNameService.JDK_NET_HOSTS_FILE, someValue);
    assertThat(new HostsFileNameService().getJdkNetHostsFile(), is(someValue));
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

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void shouldReloadIfFileGoesMissing() throws IOException {
    final String ip1 = "10.10.10.10";
    final InetAddress address1 = InetAddress.getByName(ip1);
    final Name address1Name = NameFactory.getName(address1.getAddress());
    final String ip2 = "10.10.10.20";
    final InetAddress address2 = InetAddress.getByName(ip2);
    final Name address2Name = NameFactory.getName(address2.getAddress());
    final String serverName = "The10thServer";
    final Name name = new Name(serverName);

    File file = createTempHostsFile(ip1, serverName);

    System.setProperty(HostsFileNameService.JDK_NET_HOSTS_FILE, file.getAbsolutePath());

    // file created with 10.10.10.10 The10thServer
    final HostsFileNameService hostsFileNameService = new HostsFileNameService();
    final InetAddress[] hostAddr = hostsFileNameService.lookupAllHostAddr(name);
    assertThat(hostAddr, is(new InetAddress[]{address1}));
    assertThat(hostsFileNameService.getHostByAddr(address1Name), is(serverName));
    assertThat(hostsFileNameService.getHostByAddr(address2Name), nullValue());

    // file is now gone.
    file.delete();
    assertThat(hostsFileNameService.lookupAllHostAddr(name), nullValue());
    assertThat(hostsFileNameService.getHostByAddr(address1Name), nullValue());
    assertThat(hostsFileNameService.getHostByAddr(address2Name), nullValue());

    // file re-created with 10.10.10.20 The10thServer
    writeEntryToFile(ip2, serverName, file);
    assertThat(hostsFileNameService.lookupAllHostAddr(name), is(new InetAddress[]{address2}));
    assertThat(hostsFileNameService.getHostByAddr(address1Name), nullValue());
    assertThat(hostsFileNameService.getHostByAddr(address2Name), is(serverName));
    file.delete();
  }

  private File createTempHostsFile(String ip1, String serverName) throws IOException {
    final String anyPrefix = anyString();
    final String extension = anyString();
    File file = File.createTempFile(anyPrefix, extension);

    writeEntryToFile(ip1, serverName, file);
    return file;
  }

  private void writeEntryToFile(String ip1, String serverName, File file) throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
    bos.write((ip1 + " " + serverName).getBytes());
    bos.close();
  }

  private String anyString() {
    StringBuilder randomValue = new StringBuilder();
    while (randomValue.length() < 5)
      randomValue.append(RandomFactory.getRandomValue(String.class));
    return randomValue.toString();
  }
}
