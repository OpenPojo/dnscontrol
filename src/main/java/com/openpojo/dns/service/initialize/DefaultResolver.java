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

import com.openpojo.dns.DnsControl;
import com.openpojo.dns.routing.RoutingTable;
import com.openpojo.dns.routing.impl.RoutingTableBuilder;
import com.openpojo.log.Logger;
import com.openpojo.log.LoggerFactory;

/**
 * @author oshoukry
 */
public class DefaultResolver implements Initializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResolver.class);
  private static final DnsControl DNS_CONTROL = DnsControl.getInstance();

  public DefaultResolver() {
  }

  public void init() {
    initializeAndRegisterRoutingResolver();
  }

  private void initializeAndRegisterRoutingResolver() {
    String[] nameServers = parseNameServers();
    if (nameServers != null) {
      RoutingTable routingTable = RoutingTableBuilder.create().with(null, nameServers).build();
      DNS_CONTROL.setRoutingTable(routingTable);
      DNS_CONTROL.registerRoutingResolver();
    }
    LOGGER.debug("Default DNS Servers override set to [{0}]", (Object) nameServers);
  }

  private String[] parseNameServers() {
    String nameServersConfig = System.getProperty(SUN_NET_SPI_NAMESERVICE_NAMESERVERS);
    if (nameServersConfig != null) return nameServersConfig.split(",");
    return null;
  }
}
