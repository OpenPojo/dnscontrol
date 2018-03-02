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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class DefaultIPv6PreferenceTest {
  private DefaultIPv6Preference defaultIPv6Preference;
  private String preTestIPV6PropertyValue;

  @Before
  public void setup() {
    defaultIPv6Preference = new DefaultIPv6Preference();
    preTestIPV6PropertyValue = System.getProperty(Initializer.JAVA_NET_PREFER_IPV6_ADDRESSES);
    System.clearProperty(Initializer.JAVA_NET_PREFER_IPV6_ADDRESSES);
  }

  @After
  public void teardown() {
    if (preTestIPV6PropertyValue != null)
      System.setProperty(Initializer.JAVA_NET_PREFER_IPV6_ADDRESSES, preTestIPV6PropertyValue);
    else
      System.clearProperty(Initializer.JAVA_NET_PREFER_IPV6_ADDRESSES);
  }

  @Test
  public void initialValueShouldBeFalse() {
    assertThat(defaultIPv6Preference.get(), is(false));
  }

  @Test
  public void postInitValueShouldBeFalse() {
    defaultIPv6Preference.init();
    assertThat(defaultIPv6Preference.get(), is(false));
  }

  @Test
  public void shouldParseEnvironmentVariable() {
    System.setProperty(Initializer.JAVA_NET_PREFER_IPV6_ADDRESSES, "true");
    defaultIPv6Preference.init();
    assertThat(defaultIPv6Preference.get(), is(true));
  }

  @Test
  public void shouldNotUpdateValueIfInitNotCalled() {
    defaultIPv6Preference.init();
    System.setProperty(Initializer.JAVA_NET_PREFER_IPV6_ADDRESSES, "true");
    assertThat(defaultIPv6Preference.get(), is(false));

  }
}