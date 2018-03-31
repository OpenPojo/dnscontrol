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

import java.util.List;
import java.util.Map;

/**
 * @author oshoukry
 */
public interface DnsConfigReader {
  String DEFAULT_CONFIG_FILE = "dnscontrol.conf";
  String CONFIG_FILE_ENV_VARIABLE = "dnscontrol.conf.file";
  String CONFIG_VALUES_SEPARATOR = ",";
  String CONFIG_SERVER_SYSTEM = "SYSTEM";

  String ENV_NAME_SERVERS_KEY = "sun.net.spi.nameservice.nameservers";

  /**
   * The configuration returned is in the form of key value pairs.
   */
  Map<String, List<String>> getConfiguration();

  boolean hasConfiguration();
}
