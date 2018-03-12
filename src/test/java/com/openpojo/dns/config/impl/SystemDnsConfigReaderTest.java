package com.openpojo.dns.config.impl;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import static com.openpojo.dns.routing.RoutingTable.DOT;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class SystemDnsConfigReaderTest {
  @Test
  public void canReadSystemDNSEntries() {
    SystemDnsConfigReader systemDnsConfigReader = new SystemDnsConfigReader();
    final Map<String, List<String>> configuration = systemDnsConfigReader.getConfiguration();
    assertThat(configuration, notNullValue());
    assertThat(configuration.size(), is(1));
    final List<String> defaultRoute = configuration.get(DOT);
    assertThat(defaultRoute, notNullValue());
    assertThat(defaultRoute.size(), greaterThan(0));
    for (String server : defaultRoute) {
      assertThat(server, notNullValue());
      assertThat("Found entry [" + server + "] which isn't an IPAddress", validateIPAddress(server), is(true));
    }
  }

  private boolean validateIPAddress(String ipAddress) {
    String[] tokens = ipAddress.split("\\.");
    if (tokens.length != 4) {
      return false;
    }
    for (String str : tokens) {
      int i = Integer.parseInt(str);
      if ((i < 0) || (i > 255)) {
        return false;
      }
    }
    return true;
  }
}
