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

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openpojo.dns.config.DnsConfigReader;

import static com.openpojo.dns.config.utils.ServerParser.getServersAsList;
import static com.openpojo.dns.routing.RoutingTable.DOT;
import static org.xbill.DNS.ResolverConfig.getCurrentConfig;

public class SystemDnsConfigReader implements DnsConfigReader {

  @Override
  public Map<String, List<String>> getConfiguration() {
    Map<String, List<String>> resolvConf = new HashMap<>();

    if (hasConfiguration())
      resolvConf.put(DOT, getServersAsList(systemServers()));

    return resolvConf;
  }

  @Override
  public boolean hasConfiguration() {
    return systemServers().length > 0;
  }

  /**
   * dnsjava 3.x reports the system servers as socket addresses. Keep the historical address-string form
   * (port 53 implied) that the routing table builder expects.
   */
  private static String[] systemServers() {
    final List<InetSocketAddress> servers = getCurrentConfig().servers();
    if (servers == null)
      return new String[0];
    return servers.stream().map(server -> server.getAddress().getHostAddress()).toArray(String[]::new);
  }
}
