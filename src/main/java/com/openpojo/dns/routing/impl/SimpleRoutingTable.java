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

import java.util.List;

import com.openpojo.dns.routing.RoutingTable;

/**
 * @author oshoukry
 */
public class SimpleRoutingTable implements RoutingTable {
  private final List<HostRoute> hostRoutes;
  private final List<TopLevelDomainRoute> topLevelDomainRoutes;
  private final List<DefaultRoute> defaultRoutes;

  public SimpleRoutingTable(List<HostRoute> hostRoutes, List<TopLevelDomainRoute> topLevelDomainRoutes, List<DefaultRoute> defaultRoutes) {
    this.hostRoutes = hostRoutes;
    this.topLevelDomainRoutes = topLevelDomainRoutes;
    this.defaultRoutes = defaultRoutes;
  }

  @Override
  public List<HostRoute> getHostRoutes() {
    return hostRoutes;
  }

  @Override
  public List<TopLevelDomainRoute> getTopLevelDomainRoutes() {
    return topLevelDomainRoutes;
  }

  @Override
  public List<DefaultRoute> getDefaultRoutes() {
    return defaultRoutes;
  }
}
