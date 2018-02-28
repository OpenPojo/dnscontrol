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

package com.openpojo.dns.resolve;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.openpojo.dns.exception.ResolveException;
import org.xbill.DNS.*;

/**
 * @author oshoukry
 */
public class SimpleNameServiceLookup implements NameServiceLookup {
  private boolean IPv6Preference;

  public SimpleNameServiceLookup() {
    this(false);
  }

  public SimpleNameServiceLookup(boolean IPv6Preference) {
    this.IPv6Preference = IPv6Preference;
  }

  public boolean getIPv6Preference() {
    return IPv6Preference;
  }

  @Override
  public InetAddress[] lookupAllHostAddr(String hostName) {
    Name name = getName(hostName);

    Record[] records = getRecords(name);

    if (records == null)
      throw ResolveException.getInstance("Unknown host [" + hostName + "]");

    InetAddress[] array = extractInetAddresses(records);
    return array;
  }

  private Name getName(String hostName) {
    Name name;
    try {
      name = new Name(hostName);
    } catch (NullPointerException | TextParseException e) {
      throw ResolveException.getInstance("Failed to parse name [" + hostName + "]", e);
    }

    return name;
  }

  private InetAddress[] extractInetAddresses(Record[] records) {
    InetAddress[] inetAddresses = new InetAddress[records.length];

    for (int i = 0; i < records.length; i++) {
      inetAddresses[i] = extractInetAddress(records[i]);
    }
    return inetAddresses;
  }

  private InetAddress extractInetAddress(Record record) {
    if (record instanceof ARecord) {
      return ((ARecord) record).getAddress();
    }
    return ((AAAARecord) record).getAddress();
  }

  private Record[] getRecords(Name name) {
    Record[] records;
    if (IPv6Preference)
      records = getIPv6_FallbackToIPv4(name);
    else
      records = getIPv4_FallbackToIPv6(name);

    return records;
  }

  private Record[] getIPv4_FallbackToIPv6(Name name) {
    Record[] records = new Lookup(name, Type.A).run();
    if (records == null)
      //TODO: untestable at this time need a host that is strictly IPv6 (or setup a DNS Server)
      records = new Lookup(name, Type.AAAA).run();

    return records;
  }

  private Record[] getIPv6_FallbackToIPv4(Name name) {
    Record[] records = new Lookup(name, Type.AAAA).run();
    if (records == null)
      records = new Lookup(name, Type.A).run();

    return records;
  }

  @Override
  public String getHostByAddr(byte[] addr) {
    Name name;
    try {
      name = ReverseMap.fromAddress(InetAddress.getByAddress(addr));
    } catch (UnknownHostException e) {
      throw ResolveException.getInstance("Unknown host " + Arrays.toString(addr), e);
    }
    Record[] records = new Lookup(name, Type.PTR).run();
    if (records == null)
      throw ResolveException.getInstance("Unknown host " + Arrays.toString(addr));
    return ((PTRRecord) records[0]).getTarget().toString();
  }
}
