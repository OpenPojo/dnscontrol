/*
 * Copyright (c) 2018-2026 Osman Shoukry
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

package com.openpojo.dns.cache;

import java.security.Security;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

/**
 * The JDK's own host name cache, which sits above any resolver provider and so above DNSControl.
 */
public final class JdkAddressCache {
  private static final Logger LOGGER = Logger.getLogger(JdkAddressCache.class.getName());

  /** The java.security property for how long the JDK caches a host name, in seconds; it wins over the system property. */
  public static final String NETWORKADDRESS_CACHE_TTL = "networkaddress.cache.ttl";
  /** The system property for the same, used when java.security does not set one. */
  public static final String SUN_NET_INETADDR_TTL = "sun.net.inetaddr.ttl";

  private final UnaryOperator<String> securityProperties;
  private final UnaryOperator<String> systemProperties;

  /** The cache as configured for this JVM, through java.security and system properties. */
  public static JdkAddressCache forThisJvm() {
    return new JdkAddressCache(Security::getProperty, System::getProperty);
  }

  JdkAddressCache(UnaryOperator<String> securityProperties, UnaryOperator<String> systemProperties) {
    this.securityProperties = securityProperties;
    this.systemProperties = systemProperties;
  }

  /** Logs one warning when nothing configures the JDK cache, so it holds every name for its 30 s default. */
  public void warnIfAtDefault() {
    if (isAtDefault())
      LOGGER.warning("The JDK caches every host name for 30 s by default, above DNSControl, so DNS record TTLs are not"
          + " honoured. Set -Dsun.net.inetaddr.ttl=0 (or networkaddress.cache.ttl=0 in java.security) to let DNSControl's"
          + " TTL-respecting cache decide.");
  }

  private boolean isAtDefault() {
    return securityProperties.apply(NETWORKADDRESS_CACHE_TTL) == null && systemProperties.apply(SUN_NET_INETADDR_TTL) == null;
  }
}
