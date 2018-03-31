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

package com.openpojo.dns.service.lookup.impl;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.openpojo.dns.service.java.reflection.LocalHostInfo;
import com.openpojo.dns.service.lookup.HostMapNameService;
import org.xbill.DNS.Name;

import static com.openpojo.dns.service.lookup.util.NameFactory.getName;

/**
 * @author oshoukry
 */
public class SimpleHostMapNameService implements HostMapNameService {
  private final static Logger LOGGER = Logger.getLogger(SimpleHostMapNameService.class.getName());
  private final Map<Name, InetAddress[]> forwardLookupMap = new HashMap<>();
  private final Map<Name, String> reverseLookupMap = new HashMap<>();

  @Override
  public InetAddress[] lookupAllHostAddr(Name name) {
    return forwardLookupMap.get(name);
  }

  @Override
  public String getHostByAddr(Name name) {
    return reverseLookupMap.get(name);
  }

  public SimpleHostMapNameService() {
    init();
  }

  public void init() {
    LOGGER.info("Initializing SimpleHostMapNameService()");
    addHostWithAddresses(LocalHostInfo.getHostName(), LocalHostInfo.getAllAddresses());
    addHostWithAddresses(LocalHostInfo.getLocalHostName(), LocalHostInfo.getLoopBackAddresses());
  }

  public void addHostWithAddresses(String hostName, InetAddress[] allAddresses) {
    forwardLookupMap.put(getName(hostName), allAddresses);
    for (InetAddress address : allAddresses) {
      reverseLookupMap.put(getName(address), hostName);
    }
  }

  @Override
  public String toString() {
    return "SimpleHostMapNameService{" +
        "forwardLookupMap=" + forwardMapToString() +
        ", reverseLookupMap=" + reverseLookupMap +
        '}';
  }

  private String forwardMapToString() {
    StringBuilder stringBuilder = new StringBuilder();
    boolean firstValue = true;
    for (Map.Entry<Name, InetAddress[]> entry : forwardLookupMap.entrySet()) {
      if (firstValue)
        firstValue = false;
      else
        stringBuilder.append(", ");

      stringBuilder
          .append(entry.getKey())
          .append("=")
          .append(Arrays.toString(entry.getValue()));
    }
    return stringBuilder.toString();
  }
}
