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

package com.openpojo.dns.service.java;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Logger;

import com.openpojo.dns.service.lookup.SimpleNameServiceLookup;
import com.openpojo.dns.service.initialize.DefaultDomain;
import com.openpojo.dns.service.initialize.DefaultIPv6Preference;
import com.openpojo.dns.service.initialize.DefaultResolver;
import sun.net.spi.nameservice.NameService;

/**
 * @author oshoukry
 */
public class JavaNameService implements NameService {
  private final SimpleNameServiceLookup nameServiceLookup;
  private final static Logger LOGGER = Logger.getLogger(JavaNameService.class.getName());

  public JavaNameService(DefaultDomain defaultDomain, DefaultIPv6Preference ipV6Preference, DefaultResolver resolver) {
    defaultDomain.init();
    ipV6Preference.init();
    resolver.init();
    nameServiceLookup = new SimpleNameServiceLookup(ipV6Preference.get());
    LOGGER.info("Java name service initialized");
  }

  @Override
  public InetAddress[] lookupAllHostAddr(String name) throws UnknownHostException {
    LOGGER.info("lookupAllHostAddr(" + name + ")");
    return nameServiceLookup.lookupAllHostAddr(name);
  }

  @Override
  public String getHostByAddr(byte[] addr) throws UnknownHostException {
    LOGGER.info("getHostByAddr(" + Arrays.asList(addr) + ")");
    return nameServiceLookup.getHostByAddr(addr);
  }
}
