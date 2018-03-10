package com.openpojo.dns.config.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.config.props.PropertiesLoader;
import com.openpojo.dns.exception.ConfigurationFileNotFoundException;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

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
  private final PropertiesLoader propertiesLoader;

  public FileDnsConfigReader(String fileName) {
    this.propertiesLoader = new PropertiesLoader(fileName);
    if (!propertiesLoader.exists())
      throw ConfigurationFileNotFoundException.getInstance("Configuration file [" + propertiesLoader.getFileName() + "] not found");
  }

  @Override
  public Map<String, List<String>> getConfiguration() {
    propertiesLoader.load();
    final Map<String, String> allProperties = propertiesLoader.getAllProperties();
    final Map<String, List<String>> routingEntries = new HashMap<>();
    for (Map.Entry<String, String> entry : allProperties.entrySet())
      routingEntries.put(entry.getKey(), splitServers(entry.getValue()));
    return routingEntries;
  }

  private List<String> splitServers(String value) {
    if (value == null || value.length() == 0)
      return Collections.emptyList();

    if (value.indexOf(CONFIG_VALUES_SEPARATOR) > 0)
      return asList(value.split(CONFIG_VALUES_SEPARATOR));

    return singletonList(value);
  }
}
