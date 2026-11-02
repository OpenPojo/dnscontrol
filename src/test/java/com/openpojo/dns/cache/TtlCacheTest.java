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

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;

import com.openpojo.dns.DnsControl;
import com.openpojo.dns.routing.impl.RoutingTableBuilder;
import com.openpojo.dns.testing.FakeDnsServer;
import com.openpojo.dns.testing.SteppingClock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.ClockedCache;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Lookups routed by DNSControl are cached by dnsjava for each record's own TTL, measured on a test clock.
 */
class TtlCacheTest {
  private static final Name HOST = name("ttl.a.test.");

  private final SteppingClock clock = new SteppingClock();
  private FakeDnsServer server;

  @BeforeEach
  void routeTheTestDomainToAFakeServer() throws Exception {
    Lookup.refreshDefault();
    server = FakeDnsServer.start();
    DnsControl.recreateInstance().setRoutingTable(RoutingTableBuilder.create()
        .with(".a.test", List.of(server.hostAndPort()))
        .build());
    Lookup.setDefaultCache(ClockedCache.create(clock, DClass.IN), DClass.IN);
  }

  @AfterEach
  void restoreDnsjavaDefaults() {
    server.close();
    Lookup.refreshDefault();
    DnsControl.recreateInstance();
  }

  @Test
  void shouldAnswerFromTheCacheUntilTheRecordTtlExpires() throws Exception {
    server.addRecord(new ARecord(HOST, DClass.IN, 1, InetAddress.getByName("10.0.0.1")));

    lookup(HOST);
    clock.advance(Duration.ofMillis(900));
    lookup(HOST);
    assertThat(server.queries(), hasSize(1));

    clock.advance(Duration.ofMillis(200));
    lookup(HOST);
    assertThat(server.queries(), hasSize(2));
  }

  private static void lookup(Name name) {
    new Lookup(name, Type.A).run();
  }

  private static Name name(String name) {
    try {
      return Name.fromString(name);
    } catch (Exception e) {
      throw new IllegalArgumentException(name, e);
    }
  }
}
