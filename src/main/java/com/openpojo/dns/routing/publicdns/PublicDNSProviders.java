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

package com.openpojo.dns.routing.publicdns;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.openpojo.dns.exception.ConfigException;

/**
 * @author oshoukry
 */
public class PublicDNSProviders {
  public static final String META_INF_PUBLIC_DNS_SERVERS_PROPERTIES = "META-INF/public.dns.servers.properties";

  private String configFile;
  private Properties providers;

  public PublicDNSProviders() {
    this(META_INF_PUBLIC_DNS_SERVERS_PROPERTIES);
  }

  public PublicDNSProviders(String configFile) {
    this.configFile = configFile;
    initProviders();
  }

  private void initProviders() {
    try {
      providers = new Properties();
      providers.load(getConfigAsStream());
    } catch (IOException e) {
      throw ConfigException.getInstance("Failed to load configuration file [" + configFile + "]", e);
    }
  }

  private InputStream getConfigAsStream() throws IOException {
    final InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(configFile);
    if (systemResourceAsStream == null)
      throw new IOException("Couldn't find config file [" + configFile + "]");
    return systemResourceAsStream;
  }

  public String get(String key) {
    return providers.getProperty(key);
  }
}
