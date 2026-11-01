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

import java.nio.file.Files;
import java.nio.file.Path;

import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.testing.FakeDnsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * DnsControl as a plugin on dnsjava: install() wires the routing table, hosts file and caches into Lookup so that any
 * dnsjava user, and later the JVM resolver provider, gets DNSControl's behaviour.
 */
class DnsControlInstallTest {
  private FakeDnsServer domainServer;

  @BeforeEach
  void configureARouteToAFakeServer() throws Exception {
    Lookup.refreshDefault();
    domainServer = FakeDnsServer.start();
    Path config = Files.createTempFile("dnscontrol", ".conf");
    Files.writeString(config, ".a.test=" + domainServer.hostAndPort() + "\n");
    System.setProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE, config.toString());
  }

  @AfterEach
  void restoreDnsjavaDefaults() {
    System.clearProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE);
    Lookup.refreshDefault();
    DnsControl.recreateInstance();
    domainServer.close();
  }

  @Test
  void shouldRouteLookupsThroughTheConfiguredRoutingTable() throws Exception {
    DnsControl.install();

    Record question = Record.newRecord(Name.fromString("host.a.test."), Type.A, DClass.IN);
    new Lookup(question.getName(), Type.A).run();

    assertThat(domainServer.queries(), contains(question));
  }
}
