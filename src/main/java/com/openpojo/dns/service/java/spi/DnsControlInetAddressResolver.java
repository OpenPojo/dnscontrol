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

package com.openpojo.dns.service.java.spi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * Answers the JDK's lookups through dnsjava's {@link Lookup}, which DNSControl has already wired with the routing
 * table, hosts file and search path. {@code localhost} and the machine's own name stay with the built-in resolver:
 * DNS knows nothing better about them.
 */
final class DnsControlInetAddressResolver implements InetAddressResolver {
  private final Supplier<InetAddressResolver> builtin;
  private final Supplier<String> localHostName;

  DnsControlInetAddressResolver(Supplier<InetAddressResolver> builtin, Supplier<String> localHostName) {
    this.builtin = builtin;
    this.localHostName = localHostName;
  }

  @Override
  public Stream<InetAddress> lookupByName(String host, LookupPolicy policy) throws UnknownHostException {
    if (host.equalsIgnoreCase("localhost") || host.equalsIgnoreCase(localHostName.get()))
      return builtin.get().lookupByName(host, policy);

    Name name = nameOf(host);
    int wanted = policy.characteristics();
    boolean v6First = (wanted & LookupPolicy.IPV6_FIRST) != 0;
    List<InetAddress> addresses = new ArrayList<>();
    for (int type : v6First ? new int[] { Type.AAAA, Type.A } : new int[] { Type.A, Type.AAAA })
      if ((wanted & (type == Type.A ? LookupPolicy.IPV4 : LookupPolicy.IPV6)) != 0)
        addresses.addAll(addressesOf(name, type));

    if (addresses.isEmpty())
      throw new UnknownHostException(host);
    return addresses.stream();
  }

  @Override
  public String lookupByAddress(byte[] address) throws UnknownHostException {
    Name reverseName = ReverseMap.fromAddress(InetAddress.getByAddress(address));
    Record[] records = new Lookup(reverseName, Type.PTR).run();
    if (records == null || records.length == 0)
      throw new UnknownHostException("No PTR record for " + reverseName);
    return ((PTRRecord) records[0]).getTarget().toString();
  }

  private static List<InetAddress> addressesOf(Name name, int type) {
    List<InetAddress> addresses = new ArrayList<>();
    Record[] records = new Lookup(name, type).run();
    if (records != null)
      for (Record record : records)
        addresses.add(type == Type.A ? ((ARecord) record).getAddress() : ((AAAARecord) record).getAddress());
    return addresses;
  }

  private static Name nameOf(String host) throws UnknownHostException {
    try {
      return Name.fromString(host);
    } catch (TextParseException e) {
      throw new UnknownHostException(host);
    }
  }
}
