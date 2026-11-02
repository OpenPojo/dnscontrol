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

import java.util.logging.Logger;

/**
 * The JDK's own host name cache, which sits above any resolver provider and so above DNSControl.
 */
public final class JdkAddressCache {
  private static final Logger LOGGER = Logger.getLogger(JdkAddressCache.class.getName());

  public static void warnIfAtDefault() {
    if ("0".equals(System.getProperty("sun.net.inetaddr.ttl")))
      return;
    LOGGER.warning("The JDK caches every host name for 30 s by default, above DNSControl, so DNS record TTLs are not"
        + " honoured. Set -Dsun.net.inetaddr.ttl=0 (or networkaddress.cache.ttl=0 in java.security) to let DNSControl's"
        + " TTL-respecting cache decide.");
  }

  private JdkAddressCache() { }
}
