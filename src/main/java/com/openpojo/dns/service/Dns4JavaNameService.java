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

package com.openpojo.dns.service;

import java.net.InetAddress;

import com.openpojo.dns.resolve.SimpleNameServiceLookup;
import com.openpojo.dns.service.initialize.DefaultDomain;
import com.openpojo.dns.service.initialize.DefaultIPv6Preference;
import com.openpojo.dns.service.initialize.DefaultResolver;
import com.openpojo.log.Logger;
import com.openpojo.log.LoggerFactory;
import sun.net.spi.nameservice.NameService;

/**
 * @author oshoukry
 */
public class Dns4JavaNameService implements NameService {
  private static final Logger LOGGER = LoggerFactory.getLogger(Dns4JavaNameService.class);
  private final SimpleNameServiceLookup nameServiceLookup;

  public Dns4JavaNameService(DefaultDomain defaultDomain, DefaultIPv6Preference ipV6Preference, DefaultResolver resolver) {
    defaultDomain.init();
    ipV6Preference.init();
    resolver.init();
    nameServiceLookup = new SimpleNameServiceLookup(ipV6Preference.get());
  }

  @Override
  public InetAddress[] lookupAllHostAddr(String name) {
    return nameServiceLookup.lookupAllHostAddr(name);
  }

  @Override
  public String getHostByAddr(byte[] addr) {
    return nameServiceLookup.getHostByAddr(addr);
  }
}
