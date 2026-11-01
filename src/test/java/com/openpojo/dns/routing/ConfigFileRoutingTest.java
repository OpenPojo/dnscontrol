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

package com.openpojo.dns.routing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.openpojo.dns.config.impl.FileDnsConfigReader;
import com.openpojo.dns.resolve.RoutingResolver;
import com.openpojo.dns.routing.impl.RoutingTableBuilder;
import com.openpojo.dns.testing.FakeDnsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

/**
 * The configuration file format, end to end: a dnscontrol.conf written against in-process servers, loaded through
 * the file reader, built into a routing table and exercised through the routing resolver.
 */
class ConfigFileRoutingTest {
  private FakeDnsServer defaultServer;
  private FakeDnsServer secondDefaultServer;
  private FakeDnsServer domainServer;
  private FakeDnsServer hostServer;
  private RoutingResolver routingResolver;

  @BeforeEach
  void routeFromAConfigFile() throws Exception {
    defaultServer = FakeDnsServer.start();
    secondDefaultServer = FakeDnsServer.start();
    domainServer = FakeDnsServer.start();
    hostServer = FakeDnsServer.start();

    Path config = Files.createTempFile("dnscontrol", ".conf");
    Files.writeString(config, String.join("\n",
        "# default servers, more than one",
        ".=" + defaultServer.hostAndPort() + "," + secondDefaultServer.hostAndPort(),
        "# whole domain",
        ".a.test=" + domainServer.hostAndPort(),
        "# single host inside that domain",
        "exact.a.test=" + hostServer.hostAndPort(),
        "# never resolve this one",
        "hidden.a.test=",
        ""));

    RoutingTable table = RoutingTableBuilder.create().with(new FileDnsConfigReader(config.toString())).build();
    routingResolver = new RoutingResolver(new SimpleResolver(defaultServer.address()));
    routingResolver.setRoutingTable(table);
  }

  @AfterEach
  void stopServers() {
    for (FakeDnsServer server : List.of(defaultServer, secondDefaultServer, domainServer, hostServer))
      server.close();
  }

  @Test
  void aDomainEntryRoutesEveryHostUnderIt() throws Exception {
    Record question = query("deep.inside.a.test.");

    assertThat(domainServer.queries(), contains(question));
    assertThat(hostServer.queries(), empty());
    assertThat(defaultQueries(), empty());
  }

  @Test
  void aHostEntryBeatsTheDomainEntryAroundIt() throws Exception {
    Record question = query("exact.a.test.");

    assertThat(hostServer.queries(), contains(question));
    assertThat(domainServer.queries(), empty());
  }

  @Test
  void anEmptyEntrySuppressesTheHostWithoutAskingAnyServer() throws Exception {
    Message response = routingResolver.send(Message.newQuery(question("hidden.a.test.")));

    assertThat(response.getSectionArray(Section.ANSWER).length, is(0));
    assertThat(domainServer.queries(), empty());
    assertThat(hostServer.queries(), empty());
    assertThat(defaultQueries(), empty());
  }

  @Test
  void anythingElseGoesToOneOfTheDefaultServers() throws Exception {
    Record question = query("host.elsewhere.test.");

    assertThat(defaultQueries(), hasItem(question));
    assertThat(domainServer.queries(), empty());
    assertThat(hostServer.queries(), empty());
  }

  private Record query(String host) throws Exception {
    Record question = question(host);
    routingResolver.send(Message.newQuery(question));
    return question;
  }

  private static Record question(String host) throws Exception {
    return Record.newRecord(Name.fromString(host), Type.A, DClass.IN);
  }

  private List<Record> defaultQueries() {
    List<Record> all = new ArrayList<>(defaultServer.queries());
    all.addAll(secondDefaultServer.queries());
    return all;
  }
}
