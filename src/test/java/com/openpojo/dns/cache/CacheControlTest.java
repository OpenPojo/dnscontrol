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

package com.openpojo.dns.cache;

import java.net.InetAddress;

import com.openpojo.dns.cache.utils.VerificationHelper;
import com.openpojo.dns.testing.FakeDnsServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import static com.openpojo.dns.cache.CacheControl.D_CLASS_TYPES;
import static com.openpojo.dns.cache.utils.VerificationHelper.verifyCacheIsEmpty;
import static java.util.Arrays.sort;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author oshoukry
 */
class CacheControlTest {

  @BeforeEach
  void setup() {
    Lookup.refreshDefault();
  }

  @Test
  void shouldClearCachePostPopulation() throws Exception {
    try (FakeDnsServer server = FakeDnsServer.start()) {
      Name host = Name.fromString("host.a.test.");
      server.addRecord(new ARecord(host, DClass.IN, 300, InetAddress.getByName("10.0.0.1")));
      Lookup.setDefaultResolver(new SimpleResolver(server.address()));
      verifyCacheIsEmpty();

      new Lookup(host, Type.A).run();
      assertThat(Lookup.getDefaultCache(DClass.IN).getSize(), is(1));

      CacheControl.resetCache();
      verifyCacheIsEmpty();
    } finally {
      Lookup.refreshDefault();
    }
  }

  @Test
  void shouldClearAllDefinedCaches() {
    final int[] actual = D_CLASS_TYPES.clone();
    final int[] expected = VerificationHelper.getDClassTypesSorted();
    sort(actual);
    assertThat(actual.length, is(expected.length));
    for (int i = 0; i < expected.length; i++)
      assertThat(actual[i], is(expected[i]));
  }
}
