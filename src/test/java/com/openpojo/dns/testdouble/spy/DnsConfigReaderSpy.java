package com.openpojo.dns.testdouble.spy;

import java.util.List;
import java.util.Map;

import com.openpojo.dns.config.DnsConfigReader;

/**
 * @author oshoukry
 */
public class DnsConfigReaderSpy implements DnsConfigReader {
  public boolean getConfigurationCalled = false;
  public Map<String, List<String>> configuration;

  @Override
  public Map<String, List<String>> getConfiguration() {
    getConfigurationCalled = true;
    return configuration;
  }
}
