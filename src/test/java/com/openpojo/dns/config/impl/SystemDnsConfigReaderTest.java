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
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static com.openpojo.dns.routing.RoutingTable.DOT;
import static com.openpojo.dns.config.utils.ServerAddressParser.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author oshoukry
 */
class SystemDnsConfigReaderTest {

  @Test
  void shouldReturnEmptyConfigurationIfSystemServersIsNull() {
    assertThat(new SystemDnsConfigReader(() -> null).hasConfiguration(), is(false));
  }

  @Test
  void shouldReturnEmptyConfigurationIfSystemServersIsEmptyArray() {
    assertThat(new SystemDnsConfigReader(Collections::emptyList).hasConfiguration(), is(false));
  }

  @Test
  void canReadSystemDNSEntries() throws Exception {
    final Map<String, List<String>> configuration = new SystemDnsConfigReader().getConfiguration();
    assertThat(configuration, notNullValue());
    assertThat(configuration.size(), is(1));
    final List<String> defaultRoute = configuration.get(DOT);
    assertThat(defaultRoute, notNullValue());
    assertThat(defaultRoute.size(), greaterThan(0));
    for (String server : defaultRoute) {
      assertThat(server, notNullValue());
      assertThat("Found entry [" + server + "] which isn't a server address", parse(server).getPort(), is(53));
    }
  }

  @Test
  void shouldKeepThePortOfEachSystemServer() throws Exception {
    List<InetSocketAddress> servers = Arrays.asList(new InetSocketAddress(InetAddress.getByName("::1"), 5353), new InetSocketAddress(InetAddress.getByName("10.0.0.1"), 53));

    assertThat(new SystemDnsConfigReader(() -> servers).getConfiguration().get(DOT), contains("[0:0:0:0:0:0:0:1]:5353", "10.0.0.1:53"));
  }
}
