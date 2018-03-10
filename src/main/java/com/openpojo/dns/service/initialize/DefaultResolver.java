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

package com.openpojo.dns.service.initialize;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.openpojo.dns.DnsControl;
import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.config.DnsConfigReaderFactory;
import com.openpojo.dns.routing.impl.RoutingTableBuilder;

import static com.openpojo.dns.config.DnsConfigReader.CONFIG_VALUES_SEPARATOR;

/**
 * @author oshoukry
 */
public class DefaultResolver implements Initializer {
  private static final DnsControl DNS_CONTROL = DnsControl.getInstance();

  public DefaultResolver() {
  }

  public void init() {
    initializeAndRegisterRoutingResolver();
  }

  private void initializeAndRegisterRoutingResolver() {
    final RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();
    final List<String> nameServers = getEnvironmentVariableNameServers();
    if (nameServers.size() > 0) {
      routingTableBuilder.with(null, nameServers);
    }

    DnsConfigReader reader = DnsConfigReaderFactory.getConfigReader();
    routingTableBuilder.with(reader);

    if (routingTableBuilder.getDestinationMap().size() > 0) {
      DNS_CONTROL.setRoutingTable(routingTableBuilder.build());
      DNS_CONTROL.registerRoutingResolver();
    }
  }

  private List<String> getEnvironmentVariableNameServers() {
    String nameServersConfig = System.getProperty(SUN_NET_SPI_NAMESERVICE_NAMESERVERS);
    if (nameServersConfig != null) return Arrays.asList(nameServersConfig.split(CONFIG_VALUES_SEPARATOR));
    return Collections.emptyList();
  }
}
