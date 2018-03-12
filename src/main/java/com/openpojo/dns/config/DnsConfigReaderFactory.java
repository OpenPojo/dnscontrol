package com.openpojo.dns.config;

import com.openpojo.dns.config.impl.EnvironmentDnsConfigReader;
import com.openpojo.dns.config.impl.FileDnsConfigReader;
import com.openpojo.dns.config.impl.NoOpDnsConfigReader;
import com.openpojo.dns.config.impl.SystemDnsConfigReader;
import com.openpojo.dns.exception.ConfigurationFileNotFoundException;

import static com.openpojo.dns.config.DnsConfigReader.CONFIG_FILE_ENV_VARIABLE;
import static com.openpojo.dns.config.DnsConfigReader.DEFAULT_CONFIG_FILE;
import static java.lang.System.*;

/**
 * @author oshoukry
 */
public class DnsConfigReaderFactory {

  public static DnsConfigReader getDnsConfigFileReader() {
    try {
      final String configFileLocation = getProperty(CONFIG_FILE_ENV_VARIABLE, DEFAULT_CONFIG_FILE);
      return new FileDnsConfigReader(configFileLocation);
    } catch (ConfigurationFileNotFoundException ignored) {}

    return new NoOpDnsConfigReader();
  }

  public static DnsConfigReader getSystemDnsConfigReader() {
    return new SystemDnsConfigReader();
  }

  public static DnsConfigReader getEnvironmentDnsConfigReader() {
    return new EnvironmentDnsConfigReader();
  }

  public static DnsConfigReader getDefaultDnsConfigReader() {
    DnsConfigReader envConfigReader = getEnvironmentDnsConfigReader();
    if (envConfigReader.hasConfiguration())
      return envConfigReader;

    return getSystemDnsConfigReader();
  }

  private DnsConfigReaderFactory() {}
}
