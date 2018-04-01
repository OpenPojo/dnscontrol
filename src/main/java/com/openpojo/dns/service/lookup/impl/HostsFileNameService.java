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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.openpojo.dns.config.impl.SimpleHostFileReader;
import com.openpojo.dns.service.lookup.HostMapNameService;
import org.xbill.DNS.Name;

import static com.openpojo.dns.service.lookup.util.NameFactory.getName;
import static com.openpojo.dns.service.lookup.util.NameFactory.getNameByAddressString;

/**
 * @author oshoukry
 */
public class HostsFileNameService implements HostMapNameService {
  private static final Logger LOGGER = Logger.getLogger(HostsFileNameService.class.getName());

  public static final String JDK_NET_HOSTS_FILE = "jdk.net.hosts.file";
  private String jdkNetHostsFile;
  private final AtomicReference<Map<Name, InetAddress[]>> forwardLookupMap = new AtomicReference<>();
  private final AtomicReference<Map<Name, String>> reverseLookupMap = new AtomicReference<>();

  public HostsFileNameService() {
    init();
  }

  public void init() {
    this.jdkNetHostsFile = System.getProperty(JDK_NET_HOSTS_FILE);
    LOGGER.info("Initializing with " + JDK_NET_HOSTS_FILE + " set to [" + this.jdkNetHostsFile + "]");
    final SimpleHostFileReader simpleHostFileReader = new SimpleHostFileReader(this.jdkNetHostsFile);
    if (simpleHostFileReader.hasConfiguration()) {
      initForwardLookupMap(simpleHostFileReader.getHostNameMap());
      initReverseLookupMap(simpleHostFileReader.getAddressNameMap());
    } else {
      forwardLookupMap.set(new HashMap<Name, InetAddress[]>());
      reverseLookupMap.set(new HashMap<Name, String>());
    }

    LOGGER.info("Initialization complete.");
  }

  private void initForwardLookupMap(Map<String, List<String>> hostNameMap) {
    Map<Name, InetAddress[]> newForwardLookupMap = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : hostNameMap.entrySet()) {
      List<InetAddress> addresses = new ArrayList<>();
      for (String addressName : entry.getValue()) {
        try {
          addresses.add(InetAddress.getByName(addressName));
        } catch (UnknownHostException ignored) {
        }
      }
      newForwardLookupMap.put(getName(entry.getKey()), addresses.toArray(new InetAddress[0]));
      LOGGER.info("Adding forward lookup entry [" + entry.getKey() + "] resolving to [" + addresses + "]");
    }
    forwardLookupMap.set(newForwardLookupMap);
  }

  private void initReverseLookupMap(Map<String, List<String>> addressNameMap) {
    Map<Name, String> newReverseLookupMap = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : addressNameMap.entrySet()) {
      final String hostName = entry.getValue().get(0);
      newReverseLookupMap.put(getNameByAddressString(entry.getKey()), hostName);
      LOGGER.info("Adding reverse lookup entry [" + entry.getKey() + "] resolving to [" + hostName + "]");
    }
    reverseLookupMap.set(newReverseLookupMap);
  }

  @Override
  public InetAddress[] lookupAllHostAddr(Name name) {
    return forwardLookupMap.get().get(name);
  }

  @Override
  public String getHostByAddr(Name name) {
    return reverseLookupMap.get().get(name);
  }

  public String getJdkNetHostsFile() {
    return jdkNetHostsFile;
  }
}
