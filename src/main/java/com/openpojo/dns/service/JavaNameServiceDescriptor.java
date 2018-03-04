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

package com.openpojo.dns.service;

import com.openpojo.dns.service.initialize.DefaultDomain;
import com.openpojo.dns.service.initialize.DefaultIPv6Preference;
import com.openpojo.dns.service.initialize.DefaultResolver;
import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;

import static com.openpojo.dns.DnsControl.SERVICE_PROVIDER;
import static com.openpojo.dns.DnsControl.SERVICE_TYPE;

/**
 * @author oshoukry
 */
public class JavaNameServiceDescriptor implements NameServiceDescriptor {
  private final JavaNameService javaNameService;

  public JavaNameServiceDescriptor() {
    javaNameService = new JavaNameService(new DefaultDomain(), new DefaultIPv6Preference(), new DefaultResolver());
  }

  public synchronized NameService createNameService() {
    return javaNameService;
  }

  public String getType() {
    return SERVICE_TYPE;
  }

  public String getProviderName() {
    return SERVICE_PROVIDER;
  }

}
