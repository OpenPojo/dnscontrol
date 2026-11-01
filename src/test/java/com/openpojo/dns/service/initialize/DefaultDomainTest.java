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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import com.openpojo.dns.exception.DomainException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.empty;
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
    System.clearProperty(Initializer.DNSCONTROL_SEARCH_DOMAIN);
    preTestSearchPath = Lookup.getDefaultSearchPath();
  }

  @AfterEach
  void teardown() {
    if (originalDomainValue != null)
      System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN, originalDomainValue);
    else
      System.clearProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN);
    System.clearProperty(Initializer.DNSCONTROL_SEARCH_DOMAIN);
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

  @Test
  void shouldReadTheSearchDomainFromTheDnscontrolProperty() {
    System.setProperty(Initializer.DNSCONTROL_SEARCH_DOMAIN, "a.test.");

    defaultDomain.init();

    assertThat(Lookup.getDefaultSearchPath().get(0).toString(), is("a.test."));
  }

  @Test
  void shouldPreferTheDnscontrolPropertyOverTheDeprecatedOne() {
    System.setProperty(Initializer.DNSCONTROL_SEARCH_DOMAIN, "new.test.");
    System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN, "old.test.");

    defaultDomain.init();

    assertThat(Lookup.getDefaultSearchPath().get(0).toString(), is("new.test."));
  }

  @Test
  void shouldWarnWhenTheDeprecatedPropertyIsUsed() {
    System.setProperty(Initializer.SUN_NET_SPI_NAMESERVICE_DOMAIN, "old.test.");
    List<LogRecord> records = capturingLogsOf(DefaultDomain.class, () -> defaultDomain.init());

    assertThat(records.size(), is(1));
    assertThat(records.get(0).getLevel(), is(Level.WARNING));
    assertThat(records.get(0).getMessage(), containsString("sun.net.spi.nameservice.domain is deprecated"));
  }

  @Test
  void shouldNotWarnWhenTheDnscontrolPropertyIsUsed() {
    System.setProperty(Initializer.DNSCONTROL_SEARCH_DOMAIN, "new.test.");

    assertThat(capturingLogsOf(DefaultDomain.class, () -> defaultDomain.init()), empty());
  }

  private static List<LogRecord> capturingLogsOf(Class<?> source, Runnable action) {
    List<LogRecord> records = new ArrayList<>();
    Logger logger = Logger.getLogger(source.getName());
    Handler handler = new Handler() {
      @Override public void publish(LogRecord record) { records.add(record); }
      @Override public void flush() { }
      @Override public void close() { }
    };
    logger.addHandler(handler);
    try {
      action.run();
    } finally {
      logger.removeHandler(handler);
    }
    return records;
  }
}
