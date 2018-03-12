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
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

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
