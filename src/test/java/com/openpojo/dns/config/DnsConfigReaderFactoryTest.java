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

import com.openpojo.dns.config.impl.FileDnsConfigReader;
import com.openpojo.dns.config.impl.NoOpDnsConfigReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.openpojo.dns.config.DnsConfigReader.CONFIG_FILE_ENV_VARIABLE;
import static com.openpojo.dns.config.DnsConfigReaderFactory.getDnsConfigFileReader;
import static com.openpojo.dns.constants.TestConstants.DNS_CONTROL_CONFIG;
import static java.lang.System.clearProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

/**
 * @author oshoukry
 */
public class DnsConfigReaderFactoryTest {

  @Before
  @After
  public void cleanup() {
    clearProperty(CONFIG_FILE_ENV_VARIABLE);
  }

  @Test
  public void shouldReturnNoOpDnsConfigReaderWhenConfigFileNotAvailable() {
    assertThat(getDnsConfigFileReader(), instanceOf(NoOpDnsConfigReader.class));
  }

  @Test
  public void shouldGet() {
    System.setProperty(CONFIG_FILE_ENV_VARIABLE, DNS_CONTROL_CONFIG);
    assertThat(getDnsConfigFileReader(), instanceOf(FileDnsConfigReader.class));
  }
}
