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

package com.openpojo.dns.config.utils;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class ServerParserTest {
  @Test
  public void shouldReturnEmptyListWhenNull() {
    final List<String> serversAsList = ServerParser.getServersAsList((String[]) null);
    assertThat(serversAsList, notNullValue());
    assertThat(serversAsList.size(), is(0));
  }

  @SuppressWarnings("RedundantArrayCreation")
  @Test
  public void shouldReturnEmptyListWhenEmptyArrayPassed() {
    final List<String> serversAsList = ServerParser.getServersAsList(new String[0]);
    assertThat(serversAsList, notNullValue());
    assertThat(serversAsList.size(), is(0));
  }

  @Test
  public void shouldReturnArrayForOnlyNonEmptyEntries() {
    String[] entries = { "1", "2", "", "3", null, "4" };
    List<String> expected = Arrays.asList("1", "2", "3", "4");
    final List<String> serversAsList = ServerParser.getServersAsList(entries);
    assertThat(serversAsList, notNullValue());
    assertThat(serversAsList, is(expected));
  }

  @Test
  public void shouldReturnEmptyCollectionIfStringIsNull() {
    final List<String> actual = ServerParser.splitServers(null);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), is(0));
  }
}
