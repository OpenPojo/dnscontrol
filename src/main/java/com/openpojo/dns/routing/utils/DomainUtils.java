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

import static com.openpojo.dns.routing.RoutingTable.DOT;

/**
 * @author oshoukry
 */
public class DomainUtils {

  public static String toDnsDomain(String domain) {
    if (domain == null)
      return DOT;

    if (!domain.endsWith(DOT))
      domain += DOT;

    StringBuilder result = new StringBuilder();
    String remaining = domain;

    int idx = remaining.lastIndexOf(DOT);

    while (idx >= 0) {
      result.append(remaining.substring(idx + 1));
      result.append(DOT);

      remaining = remaining.substring(0, idx);
      idx = remaining.lastIndexOf(DOT);
    }

    result.append(remaining);
    return result.toString();
  }

  private DomainUtils() {
  }
}
