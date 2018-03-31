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

import com.openpojo.dns.exception.DomainException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;

/**
 * @author oshoukry
 */
public class DefaultDomain implements Initializer {
  public DefaultDomain() { }

  public void init() {
    String domainConfig = System.getProperty(SUN_NET_SPI_NAMESERVICE_DOMAIN);
    if (domainConfig != null) try {
      Lookup.setDefaultSearchPath(new String[] { domainConfig });
    } catch (TextParseException e) {
      throw DomainException.getInstance("Failed to setup Default Domain using domain [" + domainConfig + "]", e);
    }
  }
}
