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

package com.openpojo.dns.service.initialize;

import com.openpojo.log.Logger;
import com.openpojo.log.LoggerFactory;

/**
 * @author oshoukry
 */
public class DefaultIPv6Preference implements Initializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIPv6Preference.class);
  private boolean preferIPv6Addresses;

  public DefaultIPv6Preference() {}

  public void init() {
    preferIPv6Addresses= Boolean.valueOf(System.getProperty(Initializer.JAVA_NET_PREFER_IPV6_ADDRESSES));
    LOGGER.debug("PreferIPv6Addresses [{0}]", preferIPv6Addresses);
  }

  public boolean get() {
    return preferIPv6Addresses;
  }
}
