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

package com.openpojo.dns.routing.publicdns;

import java.io.IOException;

import com.openpojo.dns.exception.ConfigException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class PublicDNSProvidersTest {
  private PublicDNSProviders publicDNSProviders;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    publicDNSProviders = new PublicDNSProviders();
  }

  @Test
  public void shouldBeAbleToRetrieveGoogle() {
    String entries = publicDNSProviders.get("google");
    assertThat(entries, notNullValue());
    assertThat(entries, is("8.8.8.8,8.8.4.4,2001:4860:4860::8888,2001:4860:4860::8844"));
  }

  @Test
  public void shouldBeAbleToRetrieveOpenDNS() {
    String entries = publicDNSProviders.get("opendns");
    assertThat(entries, notNullValue());
    assertThat(entries, is("208.67.220.220,208.67.222.222,2620:0:ccc::2,2620:0:ccd::2"));
  }

  @Test
  public void shouldReturnNullIfProviderIsUnknown() {
    assertThat(publicDNSProviders.get("UnknownProvider"), nullValue());
  }

  @Test
  public void shouldThrowConfigExceptionIfConfigFileLoadThrowsError() {
    final String invalidConfigFile = "InvalidConfigFile";
    thrown.expectMessage("Failed to load configuration file [" + invalidConfigFile + "]");
    thrown.expect(ConfigException.class);
    thrown.expectCause(isA(IOException.class));

    new PublicDNSProviders(invalidConfigFile);
  }
}
