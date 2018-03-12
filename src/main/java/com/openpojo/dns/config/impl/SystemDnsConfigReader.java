package com.openpojo.dns.config.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openpojo.dns.config.DnsConfigReader;

import static com.openpojo.dns.config.utils.ServerParser.getServersAsList;
import static com.openpojo.dns.routing.RoutingTable.DOT;
import static org.xbill.DNS.ResolverConfig.getCurrentConfig;

/**
 * @author oshoukry
 */
public class SystemDnsConfigReader implements DnsConfigReader {

  @Override
  public Map<String, List<String>> getConfiguration() {
    Map<String, List<String>> resolvConf = new HashMap<>();

    if (hasConfiguration())
      resolvConf.put(DOT, getServersAsList(getCurrentConfig().servers()));

    return resolvConf;
  }

  @Override
  public boolean hasConfiguration() {
    final String[] servers = getCurrentConfig().servers();
    return servers != null && servers.length > 0;
  }
}
