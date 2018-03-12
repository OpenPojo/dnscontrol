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
import com.openpojo.dns.routing.impl.RoutingTableBuilder;

import static com.openpojo.dns.config.DnsConfigReaderFactory.getDnsConfigFileReader;

/**
 * @author oshoukry
 */
public class DefaultResolver implements Initializer {
  public DefaultResolver() {
  }

  public void init() {
    final RoutingTableBuilder routingTableBuilder = RoutingTableBuilder.create();

    routingTableBuilder.with(getDnsConfigFileReader());

    if (routingTableBuilder.getDestinationMap().size() > 0) {
      final DnsControl dnsControl = DnsControl.getInstance();
      dnsControl.setRoutingTable(routingTableBuilder.build());
      dnsControl.registerRoutingResolver();
    }
  }

}
