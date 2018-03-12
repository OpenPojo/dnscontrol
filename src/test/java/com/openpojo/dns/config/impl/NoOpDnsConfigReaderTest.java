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
  public void setUp() throws Exception {
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
