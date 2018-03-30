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

package com.openpojo.dns.service.lookup;

import com.openpojo.dns.service.lookup.impl.SimpleHostMapNameService;
import com.openpojo.dns.service.lookup.util.NameFactory;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static com.openpojo.dns.service.lookup.impl.ForwardLookupWithFallBack.getIPAddresses;

/**
 * @author oshoukry
 */
public class SimpleNameServiceLookup implements NameServiceLookup {
  private boolean iPv6Preference;
  private HostMapNameService hostMapNameService;

  public SimpleNameServiceLookup(boolean iPv6Preference) {
    this(iPv6Preference, new SimpleHostMapNameService());
  }

  public SimpleNameServiceLookup(boolean iPv6Preference, HostMapNameService hostMapNameService) {
    this.iPv6Preference = iPv6Preference;
    this.hostMapNameService = hostMapNameService;
  }

  public boolean getIPv6Preference() {
    return iPv6Preference;
  }

  @Override
  public InetAddress[] lookupAllHostAddr(String hostName) throws UnknownHostException {
    final Name name = NameFactory.getName(hostName);

    InetAddress[] ipAddresses = hostMapNameService.lookupAllHostAddr(name);
    if (ipAddresses == null)
        ipAddresses = getIPAddresses(name, iPv6Preference);

    if (ipAddresses == null)
      throw new UnknownHostException("Unknown host [" + hostName + "]");

    return ipAddresses;
  }

  @Override
  public String getHostByAddr(byte[] ipAddress) throws UnknownHostException {
    final Name name = NameFactory.getName(ipAddress);
    String hostName = hostMapNameService.getHostByAddr(name);

    if (hostName == null)
      hostName = getPTRName(name);

    if (hostName == null)
      throw new UnknownHostException("Unknown IPAddress " + Arrays.toString(ipAddress));

    return hostName;
  }

  private static String getPTRName(Name ipAddress) {
    final Record[] records = new Lookup(ipAddress, Type.PTR).run();
    if (records == null)
      return null;
    return ((PTRRecord) records[0]).getTarget().toString();
  }

}
