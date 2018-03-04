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

package com.openpojo.dns;

import com.openpojo.dns.routing.RoutingResolver;
import com.openpojo.dns.routing.RoutingTable;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;

import static com.openpojo.dns.cache.CacheControl.resetCache;

/**
 * @author oshoukry
 */
public class DnsControl {
  public static String SERVICE_TYPE = "dns";
  public static String SERVICE_PROVIDER = "dnscontrol";
  private final RoutingResolver routingResolver;
  private final Resolver originalResolver;

  public static DnsControl getInstance() {
    return Instance.INSTANCE;
  }

  public void setRoutingTable(RoutingTable routingTable) {
    routingResolver.setRoutingTable(routingTable);
    resetCache();
  }

  public synchronized void registerRoutingResolver() {
    if (!(Lookup.getDefaultResolver() instanceof RoutingResolver))
      Lookup.setDefaultResolver(routingResolver);
  }

  public synchronized void unRegisterRoutingResolver() {
    Lookup.setDefaultResolver(originalResolver);
  }

  private DnsControl() {
    originalResolver = Lookup.getDefaultResolver();
    routingResolver = new RoutingResolver(originalResolver);
  }

  private static class Instance {
    private static final DnsControl INSTANCE = new DnsControl();
  }
}
