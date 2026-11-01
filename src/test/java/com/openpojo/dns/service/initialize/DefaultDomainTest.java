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

import java.util.List;
import com.openpojo.dns.exception.DomainException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.is;

/**
 * @author oshoukry
 */
class DefaultDomainTest {

  private DefaultDomain defaultDomain;
  private String originalDomainValue;
  private List<Name> preTestSearchPath;

  @BeforeEach
  void setUp() {
    defaultDomain = new DefaultDomain();
    originalDomainValue = System.getProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN);
    System.clearProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN);
    preTestSearchPath = Lookup.getDefaultSearchPath();
  }

  @AfterEach
  void teardown() {
    if (originalDomainValue != null)
      System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN, originalDomainValue);
    else
      System.clearProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN);
    Lookup.setDefaultSearchPath(preTestSearchPath);
  }

  @Test
  void shouldSetupDefaultDomainOnLookup() {
    String someDomain = "somedomain.com.";
    System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN, someDomain);
    defaultDomain.init();

    List<Name> defaultSearchPath = Lookup.getDefaultSearchPath();
    assertThat(defaultSearchPath.size(), is(1));
    assertThat(defaultSearchPath.get(0).toString(), is(someDomain));
  }

  @Test
  void shouldThrowTextParsingException() {
        System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN, "");

    DomainException thrown = assertThrows(DomainException.class, () -> defaultDomain.init());
    assertThat(thrown.getMessage(), containsString("Failed to setup Default Domain using domain []"));
  }
}