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
import com.openpojo.dns.config.props.PropertiesLoader;
import com.openpojo.dns.exception.ConfigurationFileNotFoundException;

import static com.openpojo.dns.config.utils.ServerParser.splitServers;

/**
 * This is the file based configuration, and should follow the format of properties.
 * for example:
 * someHost.com = dnsServer3,dnsServer4, ...etc.
 * .someHost.com = dnsServer5,dnsServer6
 * . = dnsServer1,dnsServer2,...etc.
 * <p>
 * Left hand has to be unique destination.
 * <p>
 * Destionations are determined as follows:
 * . (dot) means the default route for dns servers.
 * somedomain.com lookups for this host to be routed to the specified dns servers.
 * .somedomain.com any lookup for a host that ends with ".somedomain.com" to be routed to specified dns servers.
 * <p>
 * if the dns server list is blank it means suppress any lookup that matches the left hand.
 * For example:
 * ". = " means that don't do any lookups on zones / hosts not specifically specified.
 *
 * @author oshoukry
 */
public class FileDnsConfigReader implements DnsConfigReader {
  private final String fileName;

  public FileDnsConfigReader(String fileName) {
    this.fileName = fileName;
    if (!hasConfiguration())
      throw ConfigurationFileNotFoundException.getInstance("Configuration file [" + fileName + "] not found");
  }

  @Override
  public Map<String, List<String>> getConfiguration() {
    PropertiesLoader propertiesLoader = getPropertiesLoader();
    propertiesLoader.load();
    final Map<String, String> allProperties = propertiesLoader.getAllProperties();
    final Map<String, List<String>> routingEntries = new HashMap<>();
    for (Map.Entry<String, String> entry : allProperties.entrySet())
      routingEntries.put(entry.getKey(), splitServers(entry.getValue()));
    return routingEntries;
  }

  private PropertiesLoader getPropertiesLoader() {
    return new PropertiesLoader(this.fileName);
  }

  @Override
  public boolean hasConfiguration() {
    return getPropertiesLoader().exists();
  }
}
