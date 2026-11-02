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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Type;
import org.xbill.DNS.hosts.HostsFileParser;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Keeps dnsjava's hosts file parser current without letting a lookup see a reload in progress: each change to the file
 * gets a new, already loaded parser, swapped into {@link Lookup} whole. Stands in for dnsjava's own change detection
 * until dnsjava's reload is safe under concurrent lookups.
 */
public class HostsFileWatcher implements AutoCloseable {

  private final Path path;
  private final ScheduledExecutorService checker = Executors.newSingleThreadScheduledExecutor(runnable -> {
    Thread thread = new Thread(runnable, "dnscontrol-hosts-file-watcher");
    thread.setDaemon(true);
    return thread;
  });
  private FileTime loadedModificationTime;
  private boolean closed;

  /** Loads the file into dnsjava now, then re-checks it every {@code checkInterval} (at least every millisecond). */
  public static HostsFileWatcher watch(Path path, Duration checkInterval) {
    HostsFileWatcher watcher = new HostsFileWatcher(path);
    watcher.load();
    long intervalMillis = Math.max(1, checkInterval.toMillis());
    watcher.checker.scheduleWithFixedDelay(watcher::checkForChange, intervalMillis, intervalMillis, MILLISECONDS);
    return watcher;
  }

  private HostsFileWatcher(Path path) {
    this.path = path;
  }

  /** A file that is missing or unreadable keeps the current answers; the next check tries again. */
  synchronized void checkForChange() {
    if (closed)
      return;
    try {
      if (!Files.getLastModifiedTime(path).equals(loadedModificationTime))
        load();
    } catch (IOException missingOrUnreadable) {
      // Keep answering from the parser already installed.
    }
  }

  /**
   * dnsjava's own reload empties the parser's map before refilling it, and a concurrent lookup in that window misses.
   * A parser that never reloads has no such window. It is asked once here, so a file within
   * {@code dnsjava.hostsfile.max_size_bytes} (16 KiB by default) is fully read before any lookup can reach it; a larger
   * file is searched name by name, which adds to the map but never empties it.
   */
  private void load() {
    loadedModificationTime = modificationTimeOrNull();
    HostsFileParser parser = new HostsFileParser(path, false);
    try {
      parser.getAddressForHost(Name.root, Type.A);
    } catch (IOException unreadable) {
      // Like dnsjava's own parser: an unreadable file answers nothing, and a later change to it is loaded.
    }
    Lookup.setDefaultHostsFileParser(parser);
  }

  private FileTime modificationTimeOrNull() {
    try {
      return Files.getLastModifiedTime(path);
    } catch (IOException missing) {
      return null;
    }
  }

  /** Stops checking; a check already under way finishes without installing anything, so uninstall can restore. */
  @Override
  public synchronized void close() {
    closed = true;
    checker.shutdownNow();
  }
}
