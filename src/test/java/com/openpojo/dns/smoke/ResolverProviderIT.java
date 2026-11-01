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

package com.openpojo.dns.smoke;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.openpojo.dns.config.DnsConfigReader;
import com.openpojo.dns.testing.FakeDnsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

/**
 * The whole point, end to end: a cold JVM with nothing but dnscontrol and its dependencies on the class path resolves
 * through the routing table via plain java.net.InetAddress, with no flags and no code.
 */
class ResolverProviderIT {
  private static final String ROUTED_HOST = "host.a.test";
  private FakeDnsServer domainServer;
  private Path config;
  private InetAddress routedAddress;

  @BeforeEach
  void routeADomainToAFakeServer() throws Exception {
    domainServer = FakeDnsServer.start();
    routedAddress = InetAddress.getByName("10.7.7.7");
    domainServer.addRecord(new ARecord(Name.fromString(ROUTED_HOST + "."), DClass.IN, 300, routedAddress));
    config = Files.createTempFile("dnscontrol", ".conf");
    Files.writeString(config, ".a.test=" + domainServer.hostAndPort() + "\n");
  }

  @AfterEach
  void stopServer() {
    domainServer.close();
  }

  @Test
  void aColdJvmResolvesThroughTheRoutingTableWithNoCodeOrFlags() throws Exception {
    List<String> output = resolveInAFreshJvm(ROUTED_HOST);

    assertThat(output, contains(routedAddress.getHostAddress()));
    assertThat(domainServer.queries(), hasItem(Record.newRecord(Name.fromString(ROUTED_HOST + "."), Type.A, DClass.IN)));
  }

  @Test
  void dnscontrolDisabledLeavesTheJvmWithItsOwnResolver() throws Exception {
    List<String> output = resolveInAFreshJvm(ROUTED_HOST, "-D" + "dnscontrol.enabled=false");

    assertThat(output, contains(UnknownHostException.class.getName()));
    assertThat(domainServer.queries(), empty());
  }

  private List<String> resolveInAFreshJvm(String host, String... extraJvmArgs) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
    command.add("-cp");
    command.add(System.getProperty("java.class.path"));
    command.add("-D" + DnsConfigReader.CONFIG_FILE_ENV_VARIABLE + "=" + config);
    command.addAll(List.of(extraJvmArgs));
    command.add(SmokeMain.class.getName());
    command.add(host);

    Process jvm = new ProcessBuilder(command).redirectErrorStream(false).start();
    List<String> output = new String(jvm.getInputStream().readAllBytes()).lines().toList();
    String stderr = new String(jvm.getErrorStream().readAllBytes());
    assertThat("JVM did not finish", jvm.waitFor(60, TimeUnit.SECONDS), is(true));
    assertThat("JVM failed: " + stderr, jvm.exitValue(), is(0));
    return output;
  }
}
