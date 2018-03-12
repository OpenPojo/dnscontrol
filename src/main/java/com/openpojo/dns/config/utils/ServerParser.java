package com.openpojo.dns.config.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.openpojo.dns.config.DnsConfigReader.CONFIG_VALUES_SEPARATOR;
import static java.util.Collections.singletonList;

/**
 * @author oshoukry
 */
public class ServerParser {

  public static List<String> splitServers(String value) {
    if (value == null || value.length() == 0)
      return Collections.emptyList();

    if (value.indexOf(CONFIG_VALUES_SEPARATOR) > 0)
      return getServersAsList(value.split(CONFIG_VALUES_SEPARATOR));

    return singletonList(value);
  }

  public static List<String> getServersAsList(String ... serverArray) {
    List<String> servers = new ArrayList<>();

    if (serverArray == null) {
      return servers;
    }

    for (String server : serverArray) {
      if (!isBlank(server))
        servers.add(server);
    }
    return servers;
  }

  private static boolean isBlank(String server) {
    return server == null || server.length() == 0;
  }

  private ServerParser() {}
}
