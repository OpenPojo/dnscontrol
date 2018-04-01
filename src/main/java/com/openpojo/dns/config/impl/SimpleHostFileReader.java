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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import com.openpojo.dns.config.HostFileEntry;
import com.openpojo.dns.config.HostFileEntryFactory;
import com.openpojo.dns.config.HostFileReader;
import com.openpojo.dns.config.props.FileStreamer;

import static java.util.logging.Level.WARNING;

/**
 * @author oshoukry
 */
public class SimpleHostFileReader implements HostFileReader {
  private static Logger LOGGER = Logger.getLogger(SimpleHostFileReader.class.getName());
  private final String fileName;

  public SimpleHostFileReader(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public Map<String, List<String>> getHostNameMap() {
    final Map<String, List<String>> hostNameMap = new HashMap<>();

    try (InputStream asStream = FileStreamer.getAsStream(fileName)) {
      Scanner scanner = new Scanner(asStream);
      while (scanner.hasNextLine()) {
        final HostFileEntry entry = HostFileEntryFactory.getEntry(scanner.nextLine());
        if (entry != null) {
          for (String host : entry.getAliases()) {
            List<String> ipAddresses = hostNameMap.get(host);
            if (ipAddresses == null)
              ipAddresses = new ArrayList<>();
            ipAddresses.add(entry.getIPAddress());
            hostNameMap.put(host, ipAddresses);
          }
        }
      }
    } catch (Exception e) {
      LOGGER.log(WARNING, "Error loading configuration from host file [" + fileName + "] ", e);
    }
    return hostNameMap;
  }

  @Override
  public Map<String, List<String>> getAddressNameMap() {
    final Map<String, List<String>> addressNameMap = new HashMap<>();

    try (InputStream asStream = FileStreamer.getAsStream(fileName)) {
      Scanner scanner = new Scanner(asStream);
      while (scanner.hasNextLine()) {
        final HostFileEntry entry = HostFileEntryFactory.getEntry(scanner.nextLine());
        if (entry != null) {
          List<String> aliases = addressNameMap.get(entry.getIPAddress());
          if (aliases == null)
            aliases = new ArrayList<>();
          aliases.addAll(entry.getAliases());
          addressNameMap.put(entry.getIPAddress(), aliases);
        }
      }
    } catch (Exception e) {
      LOGGER.log(WARNING, "Error loading configuration from host file [" + fileName + "] ", e);
    }
    return addressNameMap;
  }

  @Override
  public boolean hasConfiguration() {
    return FileStreamer.exists(fileName);
  }

  public Date lastUpdated() {
    return FileStreamer.getLastUpdatedDate(fileName);
  }
}
