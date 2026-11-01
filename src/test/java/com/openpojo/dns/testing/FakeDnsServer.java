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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;

/**
 * In-process UDP DNS server for tests. Answers from the records it has been given and records every question it
 * receives, so a test can assert which server a query reached.
 */
public final class FakeDnsServer implements AutoCloseable {
  private static final int MAX_UDP_MESSAGE = 512;

  private final DatagramSocket socket;
  private final Thread listener;
  private final List<Record> records = new CopyOnWriteArrayList<>();
  private final List<Record> queries = new CopyOnWriteArrayList<>();

  private FakeDnsServer(DatagramSocket socket) {
    this.socket = socket;
    this.listener = new Thread(this::serve, "fake-dns-" + socket.getLocalPort());
    this.listener.setDaemon(true);
  }

  /**
   * Listens on the IPv6 loopback when available. dnsjava binds its client sockets to the wildcard address, and some
   * macOS network setups refuse to connect a wildcard-bound UDP socket to 127.0.0.1 while ::1 always works.
   */
  public static FakeDnsServer start() throws IOException {
    FakeDnsServer server = new FakeDnsServer(bindLoopback());
    server.listener.start();
    return server;
  }

  private static DatagramSocket bindLoopback() throws IOException {
    try {
      return new DatagramSocket(0, InetAddress.getByName("::1"));
    } catch (IOException noIpv6Loopback) {
      return new DatagramSocket(0, InetAddress.getByName("127.0.0.1"));
    }
  }

  public InetSocketAddress address() {
    return new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort());
  }

  /** The server as a routing-table entry, e.g. {@code [0:0:0:0:0:0:0:1]:53421} or {@code 127.0.0.1:53421}. */
  public String hostAndPort() {
    InetAddress address = socket.getLocalAddress();
    String host = address instanceof Inet6Address ? "[" + address.getHostAddress() + "]" : address.getHostAddress();
    return host + ":" + socket.getLocalPort();
  }

  public void addRecord(Record record) {
    records.add(record);
  }

  public List<Record> queries() {
    return List.copyOf(queries);
  }

  @Override
  public void close() {
    socket.close();
  }

  private void serve() {
    byte[] buffer = new byte[MAX_UDP_MESSAGE];
    while (!socket.isClosed()) {
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
      try {
        socket.receive(packet);
        byte[] reply = respondTo(new Message(packet.getData())).toWire(MAX_UDP_MESSAGE);
        socket.send(new DatagramPacket(reply, reply.length, packet.getSocketAddress()));
      } catch (IOException closedOrMalformed) {
        // A closed socket ends the loop; a malformed packet is dropped like a real server would.
      }
    }
  }

  private Message respondTo(Message query) {
    Record question = query.getQuestion();
    queries.add(question);

    Message response = new Message(query.getHeader().getID());
    response.getHeader().setFlag(Flags.QR);
    response.getHeader().setFlag(Flags.AA);
    response.addRecord(question, Section.QUESTION);
    for (Record record : records)
      if (record.getName().equals(question.getName()) && record.getType() == question.getType())
        response.addRecord(record, Section.ANSWER);
    if (response.getSectionArray(Section.ANSWER).length == 0)
      response.getHeader().setRcode(Rcode.NXDOMAIN);
    return response;
  }
}
