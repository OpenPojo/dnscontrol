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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.openpojo.dns.exception.RouteSetupException;
import com.openpojo.dns.routing.impl.DefaultRoute;
import com.openpojo.dns.routing.impl.HostRoute;
import com.openpojo.dns.routing.impl.TopLevelDomainRoute;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

/**
 * @author oshoukry
 */
public class RouteFactory {
  public static Route createRoute(String destination, Resolver... resolvers) {
    if (resolvers == null || resolvers.length == 0)
      throw RouteSetupException.getInstance("Null or empty resolver list passed for destination [" + destination + "]");

    if (destination == null || destination.length() == 0) {
      return new DefaultRoute(null, resolvers);
    }

    if (topLevelDomain(destination)) {
      return new TopLevelDomainRoute(destination, resolvers);
    }

    return new HostRoute(destination, resolvers);
  }

  public static Route createRoute(String destination, String... dnsServers) {
    if (dnsServers == null)
      throw RouteSetupException.getInstance("Null server list passed for destination [" + destination + "]");

    List<Resolver> resolvers = new ArrayList<>();
    for (String server : dnsServers) {
      if (server != null && server.length() > 0)
        try {
          resolvers.add(new SimpleResolver(server));
        } catch (UnknownHostException e) {
          throw RouteSetupException.getInstance(
              "Failed to create route for destination ["
                  + destination
                  + "], while processing DNS Server ["
                  + server + "] [" + e.getClass().getName() + "]", e);
        }
    }
    if (resolvers.size() == 0) {
      throw RouteSetupException.getInstance(
          "Empty DNS server list " + Arrays.toString(dnsServers) + " passed for destination [" + destination + "]");
    }

    return createRoute(destination, resolvers.toArray(new Resolver[0]));
  }

  private static boolean topLevelDomain(String destination) {
    return destination.startsWith(".");
  }
}
