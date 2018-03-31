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

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.config.DnsConfigReaderFactory;
import com.openpojo.dns.exception.RouteSetupException;
import com.openpojo.dns.resolve.RoutingResolver;
import com.openpojo.dns.routing.RoutingTable;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;

import static com.openpojo.dns.cache.CacheControl.resetCache;
import static com.openpojo.dns.routing.RoutingTable.DOT;

/**
 * @author oshoukry
 */
public class DnsControl {
  public static final String SERVICE_TYPE = "dns";
  public static final String SERVICE_PROVIDER = "dnscontrol";

  private final RoutingResolver routingResolver;
  private final Resolver defaultResolver;

  public static DnsControl getInstance() {
    return Instance.INSTANCE.get();
  }

  public static DnsControl recreateInstance() {
    Instance.INSTANCE.set(new DnsControl());
    return Instance.INSTANCE.get();
  }

  public void setRoutingTable(RoutingTable routingTable) {
    routingResolver.setRoutingTable(routingTable);
    resetCache();
    registerRoutingResolver();
  }

  public synchronized void registerRoutingResolver() {
    if (!isRoutingResolverRegistered())
      Lookup.setDefaultResolver(routingResolver);
  }

  public synchronized boolean isRoutingResolverRegistered() {
    return routingResolver == Lookup.getDefaultResolver();
  }

  public synchronized void unRegisterRoutingResolver() {
    Lookup.setDefaultResolver(defaultResolver);
  }

  private DnsControl() {
    defaultResolver = createExtendedResolver(DnsConfigReaderFactory.getDefaultDnsConfigReader());
    routingResolver = new RoutingResolver(defaultResolver);
  }

  private ExtendedResolver createExtendedResolver(DnsConfigReader configReader) {
    try {
      return new ExtendedResolver(configReader.getConfiguration().get(DOT).toArray(new String[0]));
    } catch (UnknownHostException e) {
      throw RouteSetupException.getInstance("Unknown host defined in configuration reader [" + configReader + "] " + e.getMessage(), e);
    }
  }

  private static class Instance {
    private static final AtomicReference<DnsControl> INSTANCE = new AtomicReference<>(new DnsControl());
  }
}
