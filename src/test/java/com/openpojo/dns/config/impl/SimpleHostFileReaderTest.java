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

package com.openpojo.dns.config.impl;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author oshoukry
 */
public class SimpleHostFileReaderTest {
  @Test
  public void ifFileDoesNotExistReturnEmptyConfiguration() {
    final SimpleHostFileReader simpleHostFileReader = new SimpleHostFileReader("/path/to/hostsfile");
    assertThat(simpleHostFileReader.hasConfiguration(), is(false));

    Map<String, List<String>> configuration = simpleHostFileReader.getHostNameMap();
    assertThat(configuration, notNullValue());
    assertThat(configuration.size(), is(0));

    configuration = simpleHostFileReader.getAddressNameMap();
    assertThat(configuration, notNullValue());
    assertThat(configuration.size(), is(0));
  }

  @Test
  public void canGetHostNameMap() {
    final SimpleHostFileReader simpleHostFileReader = new SimpleHostFileReader("hosts.test.file");
    assertThat(simpleHostFileReader.hasConfiguration(), is(true));
    final Map<String, List<String>> configuration = simpleHostFileReader.getHostNameMap();
    assertThat(configuration.size(), is(12));

    for (Map.Entry<String, List<String>> entry : configuration.entrySet())
      switch (entry.getKey()) {
        case "localhost":
          assertThat(entry.getKey(), entry.getValue(), contains("127.0.0.1", "::1", "fe80::1%unknown0"));
          break;
        case "mylocalserver":
          assertThat(entry.getKey(), entry.getValue(), contains("10.0.0.1"));
          break;
        case "server1":
          assertThat(entry.getKey(), entry.getValue(), contains("127.0.0.1"));
          break;
        case "server1again":
          assertThat(entry.getKey(), entry.getValue(), contains("127.0.0.1"));
          break;
        case "server1.com":
          assertThat(entry.getKey(), entry.getValue(), contains("127.0.0.1"));
          break;
        case "server2":
          assertThat(entry.getKey(), entry.getValue(), contains("10.0.0.2"));
          break;
        case "server2.com":
          assertThat(entry.getKey(), entry.getValue(), contains("127.0.0.1"));
          break;
        case "server3":
          assertThat(entry.getKey(), entry.getValue(), contains("10.0.0.3"));
          break;
        case "server4":
          assertThat(entry.getKey(), entry.getValue(), contains("10.0.0.4"));
          break;
        case "server5.com":
        case "server50":
          assertThat(entry.getKey(), entry.getValue(), contains("10.0.0.5"));
          break;
        case "broadcasthost":
          assertThat(entry.getKey(), entry.getValue(), contains("255.255.255.255"));
          break;
        default:
          fail("Found unrecognized entry " + entry);
      }
  }

  @Test
  public void getAddressNameMap() {
    final SimpleHostFileReader simpleHostFileReader = new SimpleHostFileReader("hosts.test.file");
    assertThat(simpleHostFileReader.hasConfiguration(), is(true));
    final Map<String, List<String>> configuration = simpleHostFileReader.getAddressNameMap();
    assertThat(configuration.size(), is(9));

    for (Map.Entry<String, List<String>> entry : configuration.entrySet())
      switch (entry.getKey()) {
        case "127.0.0.1":
          assertThat(entry.getKey(), entry.getValue(), contains("localhost", "server1.com", "server1", "server1again", "server2.com"));
          break;
        case "255.255.255.255":
          assertThat(entry.getKey(), entry.getValue(), contains("broadcasthost"));
          break;
        case "::1":
          assertThat(entry.getKey(), entry.getValue(), contains("localhost"));
          break;
        case "fe80::1%unknown0":
          assertThat(entry.getKey(), entry.getValue(), contains("localhost"));
          break;
        case "10.0.0.1":
          assertThat(entry.getKey(), entry.getValue(), contains("mylocalserver"));
          break;
        case "10.0.0.2":
        assertThat(entry.getKey(), entry.getValue(), contains("server2"));
        break;
        case "10.0.0.3":
        assertThat(entry.getKey(), entry.getValue(), contains("server3"));
        break;
        case "10.0.0.4":
          assertThat(entry.getKey(), entry.getValue(), contains("server4"));
          break;
        case "10.0.0.5":
          assertThat(entry.getKey(), entry.getValue(), contains("server5.com", "server50"));
          break;
        default:
          fail("Found unrecognized entry " + entry);
      }
  }
}
