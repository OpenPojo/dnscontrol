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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.config.utils.ServerParser;

import static com.openpojo.dns.routing.RoutingTable.DOT;
import static java.lang.System.getProperty;

/**
 * @author oshoukry
 */
public class EnvironmentDnsConfigReader implements DnsConfigReader {
  @Override
  public Map<String, List<String>> getConfiguration() {
    Map<String, List<String>> envDnsServers = new HashMap<>();

    final String environmentServers = getEnvironmentServers();
    if (environmentServers != null)
      envDnsServers.put(DOT, ServerParser.splitServers(environmentServers));

    return envDnsServers;
  }

  @Override
  public boolean hasConfiguration() {
    return getEnvironmentServers() != null;
  }

  private static String getEnvironmentServers() {
    return getProperty(ENV_NAME_SERVERS_KEY);
  }

  @Override
  public String toString() {
    return this.getClass().getName();
  }
}
