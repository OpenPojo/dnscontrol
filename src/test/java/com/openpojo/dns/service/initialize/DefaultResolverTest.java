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

import com.openpojo.dns.config.DnsConfigReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class DefaultResolverTest {
  private Resolver preTestResolver;
  private DefaultResolver defaultResolver;

  private String preTestNameServers;

  @Before
  public void setup() {
    preTestResolver = Lookup.getDefaultResolver();
    preTestNameServers = System.getProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY);
    System.clearProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY);

    defaultResolver = new DefaultResolver();
  }

  @After
  public void teardown() {
    Lookup.setDefaultResolver(preTestResolver);
    if (preTestNameServers != null) System.setProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY, preTestNameServers);
    else System.clearProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY);
  }

  @Test
  public void shouldNotChangeDefaultResolver() {
    defaultResolver.init();
    assertThat(Lookup.getDefaultResolver(), sameInstance(preTestResolver));
  }
}
