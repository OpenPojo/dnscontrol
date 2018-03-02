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

package com.openpojo.dns.routing.impl;

import java.util.HashMap;
import java.util.Map;

import com.openpojo.dns.routing.RoutingTable;
import org.xbill.DNS.Resolver;

import static com.openpojo.dns.routing.utils.DomainUtils.toDnsDomain;

/**
 * @author oshoukry
 */
public class OptimizedRoutingTable implements RoutingTable {
  private final Map<String, Resolver> compiledMap = new HashMap<>();

  public OptimizedRoutingTable(Map<String, Resolver> compiledMap) {
    if (compiledMap != null)
      this.compiledMap.putAll(compiledMap);
  }

  public Resolver getResolverFor(String name) {

    String key = toDnsDomain(name);
    Resolver resolver = compiledMap.get(key);
    while(key != null && key.length() > 0 && resolver == null) {
      key = key.substring(0, key.lastIndexOf(DOT));
      resolver = compiledMap.get(key + DOT);
    }
    return resolver;
  }

}
