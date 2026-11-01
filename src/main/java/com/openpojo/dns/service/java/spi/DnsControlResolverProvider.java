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

import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;
import java.util.function.Supplier;

import com.openpojo.dns.DnsControl;

/**
 * The JEP 418 resolver provider the JDK discovers through {@code META-INF/services}. On the first InetAddress lookup
 * it installs DNSControl into dnsjava and hands the JDK a resolver that answers through dnsjava's Lookup, so every
 * JVM lookup is routed.
 * <p>
 * The JDK takes the first provider it finds on the class path. dnsjava ships one too, opt-in through
 * {@code org.dnsjava.spi.enable}; if its jar precedes this one and that flag is off, the JDK keeps its built-in
 * resolver and DNSControl never runs. Put dnscontrol first on the class path, or call {@link DnsControl#install()}
 * from the application.
 */
public final class DnsControlResolverProvider extends InetAddressResolverProvider {
  public static final String NAME = "dnscontrol";

  @Override
  public InetAddressResolver get(Configuration configuration) {
    return resolver(configuration::builtinResolver, configuration::lookupLocalHostName);
  }

  @Override
  public String name() {
    return NAME;
  }

  static InetAddressResolver resolver(Supplier<InetAddressResolver> builtin, Supplier<String> localHostName) {
    DnsControl.install();
    return new DnsControlInetAddressResolver(builtin, localHostName);
  }
}
