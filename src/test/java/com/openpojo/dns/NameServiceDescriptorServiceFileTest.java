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

package com.openpojo.dns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

import com.openpojo.dns.service.Dns4JavaNameServiceDescriptor;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class NameServiceDescriptorServiceFileTest {
  @Test
  public void serviceDescriptorResourceFileExists() throws IOException {
    final Enumeration<URL> resources = getClass()
        .getClassLoader()
        .getResources("META-INF/services/sun.net.spi.nameservice.NameServiceDescriptor");

    boolean found = false;
    while (resources.hasMoreElements()) {
      URL entry = resources.nextElement();
      String serviceDescriptorLine = getServiceDescriptorLine(entry.openStream());
      if (serviceDescriptorLine.equals(Dns4JavaNameServiceDescriptor.class.getName())) {
        found = true;
        break;
      }
    }

    assertThat(found, is(true));
  }

  private String getServiceDescriptorLine(InputStream inputStream) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      if (!line.startsWith("#") && line.trim().length() != 0)
        return line;
    }
    throw new RuntimeException("No service descriptor line found");
  }
}
