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
import java.util.Map;

import com.openpojo.dns.exception.ConfigurationFileNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.openpojo.dns.constants.TestConstants.DNS_CONTROL_CONFIG;
import static com.openpojo.dns.routing.RoutingTable.DOT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author oshoukry
 */
public class FileConfiguratorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void whenInvalidFileShouldThrowFileNotFoundException() {
    final String someDummyFile = "someDummyFile";

    thrown.expect(ConfigurationFileNotFoundException.class);
    thrown.expectMessage("Configuration file [" + someDummyFile + "] not found");

    new FileDnsConfigReader(someDummyFile);
  }

  @Test
  public void canLoadFile() {
    FileDnsConfigReader fileDnsConfigReader = new FileDnsConfigReader(DNS_CONTROL_CONFIG);
    final Map<String, List<String>> dnsConfiguration = fileDnsConfigReader.getConfiguration();

    assertThat(dnsConfiguration, notNullValue());

    final List<String> defaultRoute = dnsConfiguration.get(DOT);
    assertThat(defaultRoute, contains("10.1.1.1", "10.2.2.2"));

    final List<String> hostOverrideRoute = dnsConfiguration.get("someHost.openpojo.com");
    assertThat(hostOverrideRoute, contains("10.3.3.3", "10.4.4.4"));

    final List<String> singleDnsServerHostOverrideRoute = dnsConfiguration.get("someOtherHost.openpojo.com");
    assertThat(singleDnsServerHostOverrideRoute, contains("10.5.5.5"));

    final List<String> domainOverrideRoute = dnsConfiguration.get(".someTopLevel.openpojo.com");
    assertThat(domainOverrideRoute, contains("10.6.6.6", "10.7.7.7"));

    final List<String> suppressedHostRoute = dnsConfiguration.get("suppressedEntry.openpojo.com");
    assertThat(suppressedHostRoute, notNullValue());
    assertThat(suppressedHostRoute.size(), is(0));

    final List<String> nonDefinedEntry = dnsConfiguration.get("notDefinedInFile");
    assertThat(nonDefinedEntry, nullValue());
  }

  @Test
  public void canLoadFromSystemFile() {
    FileDnsConfigReader fileDnsConfigReader = new FileDnsConfigReader("./src/test/files/dnscontrol.another.conf");
    final Map<String, List<String>> configuration = fileDnsConfigReader.getConfiguration();
    assertThat(configuration.size(), is(1));
    assertThat(configuration.get(DOT), notNullValue());
    assertThat(configuration.get(DOT).size(), is(1));
    assertThat(configuration.get(DOT).get(0), is("10.1.1.1:53"));
  }
}
