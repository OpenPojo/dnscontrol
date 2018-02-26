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

package com.openpojo.dns.routing;

import com.openpojo.dns.exception.RouteException;
import com.openpojo.dns.routing.impl.DefaultRoute;
import com.openpojo.dns.routing.impl.TopLevelDomainRoute;
import com.openpojo.dns.routing.impl.HostRoute;
import org.xbill.DNS.Resolver;

/**
 * @author oshoukry
 */
public class RouteFactory {
  public static Route createRoute(String destination, Resolver... resolvers) {
    if (resolvers == null || resolvers.length == 0)
      throw RouteException.getInstance("Invalid call, can't create routing without resolvers for [" + destination + "]");

    if (destination == null || destination.length() == 0) {
      return new DefaultRoute(null, resolvers);
    }

    if (topLevelDomain(destination)) {
      return new TopLevelDomainRoute(destination, resolvers);
    }

    return new HostRoute(destination, resolvers);
  }

  private static boolean topLevelDomain(String destination) {
    return destination.startsWith(".");
  }
}
