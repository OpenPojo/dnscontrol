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

package com.openpojo.dns.config.props;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PropertiesLoaderTest {

  @Test
  void shouldNotThrowExceptionWhenNull() {
    new PropertiesLoader(null).load();
  }

  @Test
  void shouldLoadTheFileOnlyOnce() throws IOException {
    Path file = Files.createTempFile("dnscontrol", ".properties");
    Files.writeString(file, "key=first");
    PropertiesLoader loader = new PropertiesLoader(file.toString());

    loader.load();
    Files.writeString(file, "key=second");
    loader.load();

    assertThat(loader.getAllProperties().get("key"), is("first"));
  }

  @Test
  void existsShouldReflectWhetherTheFileIsThere() throws IOException {
    Path file = Files.createTempFile("dnscontrol", ".properties");
    assertThat(new PropertiesLoader(file.toString()).exists(), is(true));
    Files.delete(file);
    assertThat(new PropertiesLoader(file.toString()).exists(), is(false));
  }

  @Test
  void existsShouldBeFalseForANullFileName() {
    assertThat(new PropertiesLoader(null).exists(), is(false));
  }
}
