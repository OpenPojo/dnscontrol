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

import com.openpojo.dns.exception.DomainException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class DefaultDomainTest {

  private DefaultDomain defaultDomain;
  private String originalDomainValue;
  private Name[] preTestSearchPath;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    defaultDomain = new DefaultDomain();
    originalDomainValue = System.getProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN);
    System.clearProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN);
    preTestSearchPath = Lookup.getDefaultSearchPath();
  }

  @After
  public void teardown() {
    if (originalDomainValue != null)
      System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN, originalDomainValue);
    else
      System.clearProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN);
    Lookup.setDefaultSearchPath(preTestSearchPath);
  }

  @Test
  public void shouldSetupDefaultDomainOnLookup() {
    String someDomain = "somedomain.com.";
    System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN, someDomain);
    defaultDomain.init();

    Name[] defaultSearchPath = Lookup.getDefaultSearchPath();
    assertThat(defaultSearchPath.length, is(1));
    assertThat(defaultSearchPath[0].toString(), is(someDomain));
  }

  @Test
  public void shouldThrowTextParsingException() {
    thrown.expect(DomainException.class);
    thrown.expectMessage("Failed to setup Default Domain using domain []");

    System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN, "");
    defaultDomain.init();
  }
}