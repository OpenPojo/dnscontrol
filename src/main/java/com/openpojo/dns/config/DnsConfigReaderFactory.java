package com.openpojo.dns.config;

import com.openpojo.dns.config.impl.FileDnsConfigReader;
import com.openpojo.dns.config.impl.NoOpDnsConfigReader;
import com.openpojo.dns.exception.ConfigurationFileNotFoundException;

import static com.openpojo.dns.config.DnsConfigReader.CONFIG_FILE_ENV_VARIABLE;
import static com.openpojo.dns.config.DnsConfigReader.DEFAULT_CONFIG_FILE;
import static java.lang.System.*;

/**
 * @author oshoukry
 */
public class DnsConfigReaderFactory {

  public static DnsConfigReader getConfigReader() {
    try {
      final String configFileLocation = getProperty(CONFIG_FILE_ENV_VARIABLE, DEFAULT_CONFIG_FILE);
      return new FileDnsConfigReader(configFileLocation);
    } catch (ConfigurationFileNotFoundException ignored) {}

    return new NoOpDnsConfigReader();
  }

  private DnsConfigReaderFactory() {}
}
