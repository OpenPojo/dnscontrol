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

package com.openpojo.dns.config;

import com.openpojo.dns.config.impl.EnvironmentDnsConfigReader;
import com.openpojo.dns.config.impl.FileDnsConfigReader;
import com.openpojo.dns.config.impl.NoOpDnsConfigReader;
import com.openpojo.dns.config.impl.SystemDnsConfigReader;
import com.openpojo.dns.exception.ConfigurationFileNotFoundException;

import static com.openpojo.dns.config.DnsConfigReader.CONFIG_FILE_ENV_VARIABLE;
import static com.openpojo.dns.config.DnsConfigReader.DEFAULT_CONFIG_FILE;
import static java.lang.System.getProperty;

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

  private DnsConfigReaderFactory() { }
}
