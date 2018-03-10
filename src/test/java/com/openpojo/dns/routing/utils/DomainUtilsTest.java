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

package com.openpojo.dns.routing.utils;

import org.junit.Test;

import static com.openpojo.dns.routing.RoutingTable.DOT;
import static com.openpojo.dns.routing.utils.DomainUtils.toDnsDomain;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class DomainUtilsTest {
  @Test
  public void whenNullReturnNull() {
    assertThat(toDnsDomain(null), is(DOT));
  }

  @Test
  public void whenEmptyReturnEmpty() {
    assertThat(toDnsDomain(""), is(DOT));
  }

  @Test
  public void whenNonEmptyButNoLevelsReturnAsIs() {
    final String openpojo = "openpojo";
    assertThat(toDnsDomain(openpojo), is(".openpojo"));
  }

  @Test
  public void whenDotComReturnComDot() {
    assertThat(toDnsDomain(".com"), is(".com."));
  }

  @Test
  public void whenOpenPojoDotComDotReturnDotComDotOpenPojo() {
    assertThat(toDnsDomain("openpojo.com."), is(".com.openpojo"));
  }

  @Test
  public void whenDotOpenPojoDotComDotReturnDotComDotOpenPojo() {
    assertThat(toDnsDomain(".openpojo.com."), is(".com.openpojo."));
  }

  @Test
  public void whenWwwDotOpenPojoDotComDotReturnDotComDotOpenPojoDotWww() {
    assertThat(toDnsDomain("www.openpojo.com."), is(".com.openpojo.www"));
  }
}
