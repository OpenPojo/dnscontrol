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

package com.openpojo.dns.service.initialize;

import java.net.UnknownHostException;
import java.util.Arrays;

import com.openpojo.dns.exception.ResolverException;
import com.openpojo.log.Logger;
import com.openpojo.log.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;

/**
 * @author oshoukry
 */
public class DefaultResolver implements Initializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResolver.class);

  public DefaultResolver() {}

  public void init() {
    String[] nameServers = parseNameServers();
    try {
      ExtendedResolver resolver;
      if (nameServers != null) {
        resolver = new ExtendedResolver(nameServers);
        Lookup.setDefaultResolver(resolver);
        LOGGER.debug("Default DNS Servers override set to [{0}]", (Object) nameServers);
      }
    } catch (UnknownHostException e) {
      throw ResolverException.
          getInstance("Failed to setup default DNS Resolver using name servers " + Arrays.toString(nameServers), e);
    }
  }

  private String[] parseNameServers() {
    String nameServersConfig = System.getProperty(SUN_NET_SPI_NAMESERVICE_NAMESERVERS);
    if (nameServersConfig != null) return nameServersConfig.split(",");
    return null;
  }
}
