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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class NoOpDnsConfigReaderTest {

  private NoOpDnsConfigReader noOpDnsConfigReader;

  @Before
  public void setUp() {
    noOpDnsConfigReader = new NoOpDnsConfigReader();
  }

  @Test
  public void shouldAlwaysReturnFalseForExists() {
    assertThat(noOpDnsConfigReader.hasConfiguration(), is(false));
  }

  @Test
  public void shouldReturnEmptyMap() {
    final Map<String, List<String>> configuration = noOpDnsConfigReader.getConfiguration();
    assertThat(configuration, notNullValue());
    assertThat(configuration.size(), is(0));
  }
}
