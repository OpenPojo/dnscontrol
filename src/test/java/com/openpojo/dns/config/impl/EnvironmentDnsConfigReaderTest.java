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

import com.openpojo.dns.constants.TestConstants;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.openpojo.dns.config.DnsConfigReader.ENV_NAME_SERVERS_KEY;
import static com.openpojo.dns.routing.RoutingTable.DOT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


/**
 * @author oshoukry
 */
public class EnvironmentDnsConfigReaderTest {

  private EnvironmentDnsConfigReader reader;

  @Before
  public void setup() {
    System.clearProperty(ENV_NAME_SERVERS_KEY);
    reader = new EnvironmentDnsConfigReader();
  }

  @After
  public void teardown() {
    System.clearProperty(ENV_NAME_SERVERS_KEY);
  }

  @Test
  public void shouldReturnEmptyConfigIfNoEnvironmentServersDefined() {
    assertThat(reader.getConfiguration(), notNullValue());
    assertThat(reader.hasConfiguration(), is(false));
  }

  @Test
  public void shouldReturnListOfServersTiedToDOT() {
    String servers = TestConstants.SERVER_1_IPv4_STRING + "," + TestConstants.SERVER_2_IPv4_STRING;
    System.setProperty(ENV_NAME_SERVERS_KEY, servers);
    assertThat(reader.hasConfiguration(), is(true));
    Map<String, List<String>> configuration = reader.getConfiguration();
    assertThat(configuration.size(), is(1));
    List<String> serversList = configuration.get(DOT);
    assertThat(serversList, notNullValue());
    assertThat(serversList.size(), is(2));
    assertThat(serversList, Matchers.containsInAnyOrder(servers.split(",")));
  }

  @Test
  public void toStringIsJustTheClassName() {
    assertThat(reader.toString(), is(reader.getClass().getName()));
  }
}