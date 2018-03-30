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

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Type;

/**
 * @author oshoukry
 */
public class ReverseLookup {

  public static String getHostName(byte[] addr) throws UnknownHostException {
    Name ipAddress = getName(addr);
    return getPTRName(ipAddress);
  }

  private static Name getName(byte[] addr) throws UnknownHostException {
    return ReverseMap.fromAddress(InetAddress.getByAddress(addr));
  }

  private static String getPTRName(Name ipAddress) {
    final Record[] records = new Lookup(ipAddress, Type.PTR).run();
    if (records == null)
      return null;
    return ((PTRRecord) records[0]).getTarget().toString();
  }

  private ReverseLookup() {}
}
