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

package com.openpojo.dns.config;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.openpojo.dns.config.impl.BasicHostFileEntry;

/**
 * @author oshoukry
 */
public class HostFileEntryFactory {
  private static final String SPACES = "\\s+";
  private static final Pattern PATTERN = Pattern.compile(SPACES);

  public static HostFileEntry getEntry(String hostFileLine) {
    BasicHostFileEntry entry = null;
    if (isValidConfigLine(hostFileLine)) {
      final String[] tokens = PATTERN.split(hostFileLine.trim());
      if (tokens.length > 1) {
        entry = new BasicHostFileEntry(tokens[0], Arrays.asList(tokens).subList(1, tokens.length));
      }
    }
    return entry;
  }

  private static boolean isValidConfigLine(String line) {
    return line != null && line.trim().length() > 0 && !line.trim().startsWith("#");
  }

  private HostFileEntryFactory() {
  }
}
