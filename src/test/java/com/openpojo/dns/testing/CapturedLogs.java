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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Collects what a class logs through java.util.logging while an action runs.
 */
public final class CapturedLogs {

  public static List<LogRecord> capturingLogsOf(Class<?> source, Runnable action) {
    List<LogRecord> records = new ArrayList<>();
    Logger logger = Logger.getLogger(source.getName());
    Handler handler = new Handler() {
      @Override public void publish(LogRecord record) { records.add(record); }
      @Override public void flush() { }
      @Override public void close() { }
    };
    logger.addHandler(handler);
    try {
      action.run();
    } finally {
      logger.removeHandler(handler);
    }
    return records;
  }

  private CapturedLogs() { }
}
