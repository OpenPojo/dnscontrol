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

package com.openpojo.dns;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.testing.FakeDnsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

/**
 * Lookups racing hosts-file rewrites: the file is reloaded under concurrent {@link Lookup}s, and every answer must come
 * from one whole version of the file, never from a missing entry or a half-read one.
 */
class HostsFileReloadConcurrencyTest {
  private static final int LOOKUP_THREADS = 32;
  private static final int REWRITES = 3_000;
  private static final String OLD_ADDRESS = "10.0.0.1";
  private static final String NEW_ADDRESS = "10.0.0.2";

  private FakeDnsServer domainServer;
  private Path hosts;

  @BeforeEach
  void installWithAHostsFileThatIsCheckedOnEveryLookup() throws Exception {
    Lookup.refreshDefault();
    DnsControl.recreateInstance();
    domainServer = FakeDnsServer.start();
    Path config = Files.createTempFile("dnscontrol", ".conf");
    Files.writeString(config, ".a.test=" + domainServer.hostAndPort() + "\n");
    System.setProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE, config.toString());
    hosts = Files.createTempFile("hosts", ".txt");
    replaceHostsFileWith(OLD_ADDRESS + " racing.a.test", 0);
    System.setProperty(DnsControl.JDK_NET_HOSTS_FILE, hosts.toString());
    System.setProperty(DnsControl.DNSJAVA_HOSTS_FILE_CHECK_INTERVAL, "0");
    DnsControl.install();
  }

  @AfterEach
  void restoreDnsjavaDefaults() {
    System.clearProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE);
    System.clearProperty(DnsControl.JDK_NET_HOSTS_FILE);
    System.clearProperty(DnsControl.DNSJAVA_HOSTS_FILE_CHECK_INTERVAL);
    Lookup.refreshDefault();
    DnsControl.recreateInstance();
    domainServer.close();
  }

  @Test
  void shouldAnswerEveryConcurrentLookupWithTheOldOrTheNewAddressWhileTheHostsFileIsRewritten() throws Exception {
    Set<String> answers = ConcurrentHashMap.newKeySet();
    Queue<Throwable> failures = new ConcurrentLinkedQueue<>();
    AtomicBoolean rewriting = new AtomicBoolean(true);
    CountDownLatch started = new CountDownLatch(LOOKUP_THREADS);

    ExecutorService pool = Executors.newFixedThreadPool(LOOKUP_THREADS);
    List<Future<?>> lookups = new ArrayList<>();
    for (int i = 0; i < LOOKUP_THREADS; i++)
      lookups.add(pool.submit(() -> {
        started.countDown();
        do {
          try {
            answers.add(addressOf(new Lookup("racing.a.test.", Type.A).run()));
          } catch (Throwable failure) {
            failures.add(failure);
          }
        } while (rewriting.get());
      }));

    started.await();
    for (int rewrite = 1; rewrite <= REWRITES; rewrite++) {
      replaceHostsFileWith((rewrite % 2 == 1 ? NEW_ADDRESS : OLD_ADDRESS) + " racing.a.test", rewrite);
    }
    rewriting.set(false);
    for (Future<?> lookup : lookups)
      lookup.get();
    pool.shutdown();

    assertThat(failures, empty());
    assertThat(answers, containsInAnyOrder(OLD_ADDRESS, NEW_ADDRESS));
  }

  /** Written aside and moved into place, the way hosts-file managers do, so no reload can see a half-written file. */
  private void replaceHostsFileWith(String line, int rewrite) throws Exception {
    Path next = Files.createTempFile(hosts.getParent(), "hosts", ".next");
    Files.writeString(next, line + "\n");
    Files.setLastModifiedTime(next, FileTime.from(Instant.now().plusSeconds(rewrite)));
    Files.move(next, hosts, ATOMIC_MOVE, REPLACE_EXISTING);
  }

  private static String addressOf(Record[] answers) {
    if (answers == null || answers.length != 1)
      return "no single answer";
    InetAddress address = ((ARecord) answers[0]).getAddress();
    return address.getHostAddress();
  }
}
