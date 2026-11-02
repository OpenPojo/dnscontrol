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

import java.util.Map;

import org.junit.jupiter.api.Test;

import static com.openpojo.dns.cache.JdkAddressCache.NETWORKADDRESS_CACHE_TTL;
import static com.openpojo.dns.cache.JdkAddressCache.SUN_NET_INETADDR_TTL;
import static com.openpojo.dns.testing.CapturedLogs.capturingLogsOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

class JdkAddressCacheTest {
  private static final Map<String, String> NOTHING_SET = Map.of();

  @Test
  void shouldNotWarnWhenTheJdkCacheIsTurnedOffInJavaSecurity() {
    JdkAddressCache jdkCache = new JdkAddressCache(Map.of(NETWORKADDRESS_CACHE_TTL, "0")::get, NOTHING_SET::get);

    assertThat(capturingLogsOf(JdkAddressCache.class, jdkCache::warnIfAtDefault), empty());
  }

  @Test
  void shouldNotWarnWhenTheOperatorChoseATtlWithTheSystemProperty() {
    JdkAddressCache jdkCache = new JdkAddressCache(NOTHING_SET::get, Map.of(SUN_NET_INETADDR_TTL, "60")::get);

    assertThat(capturingLogsOf(JdkAddressCache.class, jdkCache::warnIfAtDefault), empty());
  }

  @Test
  void shouldNotWarnWhenTheOperatorChoseATtlInJavaSecurity() {
    JdkAddressCache jdkCache = new JdkAddressCache(Map.of(NETWORKADDRESS_CACHE_TTL, "60")::get, NOTHING_SET::get);

    assertThat(capturingLogsOf(JdkAddressCache.class, jdkCache::warnIfAtDefault), empty());
  }
}
