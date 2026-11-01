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

import java.util.Arrays;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.ResolverConfig;

import static com.openpojo.dns.routing.RoutingTable.DOT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author oshoukry
 */
class SystemDnsConfigReaderTest {

  private SystemDnsConfigReader systemDnsConfigReader;

  @BeforeEach
  void setup() {
    ResolverConfig.refresh();
    systemDnsConfigReader = new SystemDnsConfigReader();
  }

  @AfterEach
  void teardown() {
    ResolverConfig.refresh();
  }

  @Test
  void shouldReturnEmptyConfigurationIfSystemServersIsNull() throws NoSuchFieldException, IllegalAccessException {
    setServersField(null);
    assertThat(systemDnsConfigReader.hasConfiguration(), is(false));
  }

  @Test
  void shouldReturnEmptyConfigurationIfSystemServersIsEmptyArray() throws NoSuchFieldException, IllegalAccessException {
    setServersField(Collections.<InetSocketAddress>emptyList());
    assertThat(systemDnsConfigReader.hasConfiguration(), is(false));
  }

  private void setServersField(List<InetSocketAddress> value) throws NoSuchFieldException, IllegalAccessException {
    Field serversField = ResolverConfig.class.getDeclaredField("servers");
    serversField.setAccessible(true);
    serversField.set(ResolverConfig.getCurrentConfig(), value);
  }

  @Test
  void canReadSystemDNSEntries() {
    final Map<String, List<String>> configuration = systemDnsConfigReader.getConfiguration();
    assertThat(configuration, notNullValue());
    assertThat(configuration.size(), is(1));
    final List<String> defaultRoute = configuration.get(DOT);
    assertThat(defaultRoute, notNullValue());
    assertThat(defaultRoute.size(), greaterThan(0));
    for (String server : defaultRoute) {
      assertThat(server, notNullValue());
      assertThat("Found entry [" + server + "] which isn't an IPAddress", validateIPAddress(server), is(true));
    }
  }

  private boolean validateIPAddress(String ipAddress) {
    String[] tokens = ipAddress.split("\\.");
    if (tokens.length != 4) {
      return false;
    }
    for (String str : tokens) {
      int i = Integer.parseInt(str);
      if ((i < 0) || (i > 255)) {
        return false;
      }
    }
    return true;
  }

  @Test
  void shouldKeepThePortOfEachSystemServer() throws Exception {
    setServersField(Arrays.asList(new InetSocketAddress(InetAddress.getByName("::1"), 5353), new InetSocketAddress(InetAddress.getByName("10.0.0.1"), 53)));

    assertThat(systemDnsConfigReader.getConfiguration().get(DOT), contains("[0:0:0:0:0:0:0:1]:5353", "10.0.0.1:53"));
  }
}
