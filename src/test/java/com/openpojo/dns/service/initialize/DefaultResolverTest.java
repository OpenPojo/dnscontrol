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
import com.openpojo.dns.resolve.RoutingResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class DefaultResolverTest {

  @Before
  public void setup() {
    System.clearProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY);
    System.clearProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE);
    Lookup.refreshDefault();

  }

  @After
  public void teardown() {
    System.clearProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE);
    System.clearProperty(DnsConfigReader.ENV_NAME_SERVERS_KEY);
    Lookup.refreshDefault();
  }

  @Test
  public void shouldNotChangeDefaultResolver() {
    Resolver preTestResolver = Lookup.getDefaultResolver();

    DefaultResolver defaultResolver = new DefaultResolver();
    defaultResolver.init();

    assertThat(preTestResolver, sameInstance(Lookup.getDefaultResolver()));
  }

  @Test
  public void shouldRegisterRoutingResolverIfConfigured() {
    assertThat(Lookup.getDefaultResolver(), not(instanceOf(RoutingResolver.class)));
    System.setProperty(DnsConfigReader.CONFIG_FILE_ENV_VARIABLE, "dnscontrol.test.conf");
    DefaultResolver defaultResolver = new DefaultResolver();
    defaultResolver.init();
    assertThat(Lookup.getDefaultResolver(), instanceOf(RoutingResolver.class));
  }
}
