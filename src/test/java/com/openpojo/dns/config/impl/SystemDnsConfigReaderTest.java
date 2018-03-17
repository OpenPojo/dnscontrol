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

import static com.openpojo.dns.routing.RoutingTable.DOT;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class SystemDnsConfigReaderTest {
  @Test
  public void canReadSystemDNSEntries() {
    SystemDnsConfigReader systemDnsConfigReader = new SystemDnsConfigReader();
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
}
