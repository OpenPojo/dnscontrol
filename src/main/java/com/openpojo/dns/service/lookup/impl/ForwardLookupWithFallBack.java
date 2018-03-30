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

import com.openpojo.dns.exception.ResolveException;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * @author oshoukry
 */
public class ForwardLookupWithFallBack {
  public static final int IPv4 = Type.A;
  public static final int IPv6 = Type.AAAA;

  public static InetAddress[] getIPAddresses(String hostName, boolean ipV6Preferred) {
    Name name = getName(hostName);

    if (ipV6Preferred)
      return getRecordsWithFallback(name, IPv6, IPv4);
    else
      return getRecordsWithFallback(name, IPv4, IPv6);
  }

  private static Name getName(String hostName) {
    Name name;
    try {
      name = new Name(hostName);
    } catch (NullPointerException | TextParseException e) {
      throw ResolveException.getInstance("Failed to parse name [" + hostName + "]", e);
    }

    return name;
  }

  private static InetAddress[] getRecordsWithFallback(Name name, int preferredType, int fallbackType) {
    Record[] records = new Lookup(name, preferredType).run();
    int extractedTypeInt = preferredType;
    if (records == null) {
      records = new Lookup(name, fallbackType).run();
      extractedTypeInt = fallbackType;
    }

    return extractInetAddresses(records, extractedTypeInt);
  }

  private static InetAddress[] extractInetAddresses(Record[] records, int extractedTypeInt) {
    if (records == null)
      return null;

    InetAddress[] inetAddresses = new InetAddress[records.length];

    for (int i = 0; i < records.length; i++) {
      inetAddresses[i] = extractedTypeInt == IPv4 ? ((ARecord) records[i]).getAddress() : ((AAAARecord) records[i]).getAddress();
    }
    return inetAddresses;
  }

  private ForwardLookupWithFallBack() {
  }
}
