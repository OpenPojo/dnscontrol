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

package com.openpojo.dns.hosts;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Type;
import org.xbill.DNS.hosts.HostsFileParser;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

class HostsFileWatcherTest {
  private static final Duration NOT_DURING_THE_TEST = Duration.ofHours(1);

  private Path hosts;
  private HostsFileWatcher watcher;

  @BeforeEach
  void startWithAHostsFile() throws Exception {
    Lookup.refreshDefault();
    hosts = Files.createTempFile("hosts", ".txt");
    Files.writeString(hosts, "10.0.0.1 watched.a.test\n");
  }

  @AfterEach
  void stopWatching() {
    if (watcher != null)
      watcher.close();
    Lookup.refreshDefault();
  }

  @Test
  void shouldInstallAParserThatAnswersFromTheHostsFile() throws Exception {
    watcher = HostsFileWatcher.watch(hosts, NOT_DURING_THE_TEST);

    assertThat(addressOf("watched.a.test."), is(Optional.of(InetAddress.getByName("10.0.0.1"))));
  }

  @Test
  void shouldHaveReadTheWholeFileBeforeAnyLookupSeesTheParser() throws Exception {
    watcher = HostsFileWatcher.watch(hosts, NOT_DURING_THE_TEST);

    Files.delete(hosts);

    assertThat(addressOf("watched.a.test."), is(Optional.of(InetAddress.getByName("10.0.0.1"))));
  }

  @Test
  void shouldStartWithoutTheFileAndLoadItOnceItAppears() throws Exception {
    Files.delete(hosts);
    watcher = HostsFileWatcher.watch(hosts, NOT_DURING_THE_TEST);
    assertThat(addressOf("watched.a.test."), is(Optional.empty()));

    Files.writeString(hosts, "10.0.0.4 watched.a.test\n");
    watcher.checkForChange();

    assertThat(addressOf("watched.a.test."), is(Optional.of(InetAddress.getByName("10.0.0.4"))));
  }

  @Test
  void shouldAnswerNothingFromAnUnreadableFileInsteadOfFailing() throws Exception {
    Files.setPosixFilePermissions(hosts, PosixFilePermissions.fromString("---------"));

    watcher = HostsFileWatcher.watch(hosts, NOT_DURING_THE_TEST);

    assertThat(addressOf("watched.a.test."), is(Optional.empty()));
  }

  @Test
  void shouldAnswerFromTheNewFileOnceAChangeIsSeen() throws Exception {
    watcher = HostsFileWatcher.watch(hosts, NOT_DURING_THE_TEST);
    addressOf("watched.a.test.");

    replaceHostsFileWith("10.0.0.2 watched.a.test");
    watcher.checkForChange();

    assertThat(addressOf("watched.a.test."), is(Optional.of(InetAddress.getByName("10.0.0.2"))));
  }

  @Test
  void shouldKeepTheLoadedParserWhileTheFileIsUnchanged() throws Exception {
    watcher = HostsFileWatcher.watch(hosts, NOT_DURING_THE_TEST);
    HostsFileParser loaded = Lookup.getDefaultHostsFileParser();

    watcher.checkForChange();

    assertThat(Lookup.getDefaultHostsFileParser(), sameInstance(loaded));
  }

  @Test
  void shouldKeepAnsweringWhileTheFileIsMissingAndLoadItWhenItReturns() throws Exception {
    watcher = HostsFileWatcher.watch(hosts, NOT_DURING_THE_TEST);
    FileTime before = Files.getLastModifiedTime(hosts);
    Files.delete(hosts);

    watcher.checkForChange();
    assertThat(addressOf("watched.a.test."), is(Optional.of(InetAddress.getByName("10.0.0.1"))));

    Files.writeString(hosts, "10.0.0.3 watched.a.test\n");
    Files.setLastModifiedTime(hosts, FileTime.from(before.toInstant().plusSeconds(1)));
    watcher.checkForChange();
    assertThat(addressOf("watched.a.test."), is(Optional.of(InetAddress.getByName("10.0.0.3"))));
  }

  @Test
  void shouldCheckForChangesOnItsOwnAtTheGivenInterval() throws Exception {
    watcher = HostsFileWatcher.watch(hosts, Duration.ofMillis(10));

    replaceHostsFileWith("10.0.0.2 watched.a.test");

    assertThat(addressWithin(Duration.ofSeconds(2), "watched.a.test.", "10.0.0.2"), is(Optional.of(InetAddress.getByName("10.0.0.2"))));
  }

  @Test
  void shouldCheckAsOftenAsItCanWhenTheIntervalIsZero() throws Exception {
    watcher = HostsFileWatcher.watch(hosts, Duration.ZERO);

    replaceHostsFileWith("10.0.0.2 watched.a.test");

    assertThat(addressWithin(Duration.ofSeconds(2), "watched.a.test.", "10.0.0.2"), is(Optional.of(InetAddress.getByName("10.0.0.2"))));
  }

  @Test
  void shouldNeverInstallAParserOnceClosed() throws Exception {
    watcher = HostsFileWatcher.watch(hosts, NOT_DURING_THE_TEST);
    watcher.close();
    HostsFileParser restored = new HostsFileParser(hosts);
    Lookup.setDefaultHostsFileParser(restored);

    replaceHostsFileWith("10.0.0.2 watched.a.test");
    watcher.checkForChange();

    assertThat(Lookup.getDefaultHostsFileParser(), sameInstance(restored));
  }

  @Test
  void shouldEndItsCheckingThreadWhenClosed() throws Exception {
    long threadsBefore = watcherThreads();
    watcher = HostsFileWatcher.watch(hosts, Duration.ofMillis(10));
    Thread.sleep(20);
    assertThat(watcherThreads(), is(threadsBefore + 1));

    watcher.close();

    Instant deadline = Instant.now().plusSeconds(2);
    while (watcherThreads() > threadsBefore && Instant.now().isBefore(deadline))
      Thread.sleep(5);
    assertThat(watcherThreads(), is(threadsBefore));
  }

  @Test
  void shouldNeverKeepTheJvmAlive() throws Exception {
    watcher = HostsFileWatcher.watch(hosts, Duration.ofMillis(10));
    Thread.sleep(20);

    assertThat(liveWatcherThreads().allMatch(Thread::isDaemon), is(true));
  }

  private static long watcherThreads() {
    return liveWatcherThreads().count();
  }

  private static Stream<Thread> liveWatcherThreads() {
    return Thread.getAllStackTraces().keySet().stream()
        .filter(thread -> thread.getName().equals("dnscontrol-hosts-file-watcher") && thread.isAlive());
  }

  private static Optional<InetAddress> addressWithin(Duration timeout, String name, String expected) throws Exception {
    Instant deadline = Instant.now().plus(timeout);
    Optional<InetAddress> address = addressOf(name);
    while (!address.equals(Optional.of(InetAddress.getByName(expected))) && Instant.now().isBefore(deadline)) {
      Thread.sleep(5);
      address = addressOf(name);
    }
    return address;
  }

  private void replaceHostsFileWith(String line) throws Exception {
    Path next = Files.createTempFile(hosts.getParent(), "hosts", ".next");
    Files.writeString(next, line + "\n");
    Files.setLastModifiedTime(next, FileTime.from(Files.getLastModifiedTime(hosts).toInstant().plusSeconds(1)));
    Files.move(next, hosts, ATOMIC_MOVE, REPLACE_EXISTING);
  }

  private static Optional<InetAddress> addressOf(String name) throws Exception {
    return Lookup.getDefaultHostsFileParser().getAddressForHost(Name.fromString(name), Type.A);
  }
}
