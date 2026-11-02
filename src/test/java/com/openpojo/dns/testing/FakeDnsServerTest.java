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

package com.openpojo.dns.testing;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class FakeDnsServerTest {

  @Test
  void shouldAnswerAnARecordQueryAndRecordTheQuestion() throws Exception {
    Name name = Name.fromString("host.a.test.");
    InetAddress address = InetAddress.getByName("10.0.0.1");

    try (FakeDnsServer server = FakeDnsServer.start()) {
      server.addRecord(new ARecord(name, DClass.IN, 300, address));

      Message response = query(server, name, Type.A);

      assertThat(response.getHeader().getRcode(), is(Rcode.NOERROR));
      Record[] answers = response.getSectionArray(Section.ANSWER);
      assertThat(answers.length, is(1));
      assertThat(((ARecord) answers[0]).getAddress(), is(address));
      assertThat(server.queries(), contains(Record.newRecord(name, Type.A, DClass.IN)));
    }
  }

  @Test
  void shouldAnswerNxdomainForAnUnknownNameAndStillRecordTheQuestion() throws Exception {
    try (FakeDnsServer server = FakeDnsServer.start()) {
      Name name = Name.fromString("missing.a.test.");
      Message response = query(server, name, Type.A);

      assertThat(response.getHeader().getRcode(), is(Rcode.NXDOMAIN));
      assertThat(response.getSectionArray(Section.ANSWER).length, is(0));
      assertThat(server.queries(), contains(Record.newRecord(name, Type.A, DClass.IN)));
    }
  }

  @Test
  void shouldPutTheZoneSoaInTheAuthoritySectionOfAnNxdomainAnswer() throws Exception {
    Name zone = Name.fromString("a.test.");
    SOARecord soa = new SOARecord(zone, DClass.IN, 60, Name.fromString("ns.a.test."), Name.fromString("admin.a.test."),
        1, 3600, 600, 86400, 2);
    try (FakeDnsServer server = FakeDnsServer.start()) {
      server.addRecord(soa);

      Message response = query(server, Name.fromString("missing.a.test."), Type.A);

      assertThat(response.getHeader().getRcode(), is(Rcode.NXDOMAIN));
      assertThat(response.getSectionArray(Section.AUTHORITY), arrayContaining((Record) soa));
    }
  }

  @Test
  void shouldServeAaaaRecords() throws Exception {
    Name name = Name.fromString("host6.a.test.");
    InetAddress address = InetAddress.getByName("2001:db8::1");
    try (FakeDnsServer server = FakeDnsServer.start()) {
      server.addRecord(new AAAARecord(name, DClass.IN, 300, address));

      Message response = query(server, name, Type.AAAA);

      assertThat(((AAAARecord) response.getSectionArray(Section.ANSWER)[0]).getAddress(), is(address));
    }
  }

  @Test
  void shouldServePtrRecordsForReverseLookups() throws Exception {
    Name reverseName = ReverseMap.fromAddress(InetAddress.getByName("10.0.0.1"));
    Name target = Name.fromString("host.a.test.");
    try (FakeDnsServer server = FakeDnsServer.start()) {
      server.addRecord(new PTRRecord(reverseName, DClass.IN, 300, target));

      Message response = query(server, reverseName, Type.PTR);

      assertThat(((PTRRecord) response.getSectionArray(Section.ANSWER)[0]).getTarget(), is(target));
    }
  }

  @Test
  void shouldReturnRecordsWithTheTtlTheyWereGiven() throws Exception {
    Name name = Name.fromString("ttl.a.test.");
    try (FakeDnsServer server = FakeDnsServer.start()) {
      server.addRecord(new ARecord(name, DClass.IN, 7, InetAddress.getByName("10.0.0.7")));

      Message response = query(server, name, Type.A);

      assertThat(response.getSectionArray(Section.ANSWER)[0].getTTL(), is(7L));
    }
  }

  @Test
  void shouldKeepQueriesOfTwoServersApart() throws Exception {
    Name forA = Name.fromString("only.a.test.");
    Name forB = Name.fromString("only.b.test.");
    try (FakeDnsServer serverA = FakeDnsServer.start(); FakeDnsServer serverB = FakeDnsServer.start()) {
      query(serverA, forA, Type.A);
      query(serverB, forB, Type.A);

      assertThat(serverA.queries(), contains(Record.newRecord(forA, Type.A, DClass.IN)));
      assertThat(serverB.queries(), contains(Record.newRecord(forB, Type.A, DClass.IN)));
    }
  }

  private static Message query(FakeDnsServer server, Name name, int type) throws Exception {
    return new SimpleResolver(server.address()).send(Message.newQuery(Record.newRecord(name, type, DClass.IN)));
  }
}
