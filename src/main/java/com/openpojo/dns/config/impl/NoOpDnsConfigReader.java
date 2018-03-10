package com.openpojo.dns.config.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openpojo.dns.config.DnsConfigReader;

/**
 * @author oshoukry
 */
public class NoOpDnsConfigReader implements DnsConfigReader {
  @Override
  public Map<String, List<String>> getConfiguration() {
    return new HashMap<>();
  }
}
