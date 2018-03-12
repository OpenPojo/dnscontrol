package com.openpojo.dns.config;

import java.util.List;
import java.util.Map;

/**
 * @author oshoukry
 */
public interface DnsConfigReader {
  String DEFAULT_CONFIG_FILE = "dnscontrol.conf";
  String CONFIG_FILE_ENV_VARIABLE= "dnscontrol.conf.file";
  String CONFIG_VALUES_SEPARATOR = ",";

  String ENV_NAME_SERVERS_KEY = "sun.net.spi.nameservice.nameservers";

  /**
   * The configuration returned is in the form of key value pairs.
   */
  Map<String, List<String>> getConfiguration();

  boolean hasConfiguration();
}
