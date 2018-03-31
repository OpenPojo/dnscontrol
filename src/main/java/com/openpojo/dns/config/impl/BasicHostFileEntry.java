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

package com.openpojo.dns.config.impl;

import java.util.List;

import com.openpojo.dns.config.HostFileEntry;

/**
 * @author oshoukry
 */
public class BasicHostFileEntry implements HostFileEntry {
  private final String ipAddress;
  private final List<String> aliases;

  public BasicHostFileEntry(String ipAddress, List<String> aliases) {
    this.ipAddress = ipAddress;
    this.aliases = aliases;
  }

  @Override
  public String getIPAddress() {
    return ipAddress;
  }

  @Override
  public List<String> getAliases() {
    return aliases;
  }
}
