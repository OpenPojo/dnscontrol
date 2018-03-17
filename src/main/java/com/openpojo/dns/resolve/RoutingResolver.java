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

package com.openpojo.dns.resolve;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.openpojo.dns.routing.RoutingTable;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Resolver;

/**
 * @author oshoukry
 */
public class RoutingResolver extends UnimplementedResolver {
  private final AtomicReference<RoutingTable> safeAccessRoutingTable = new AtomicReference<>();
  private final Resolver defaultSystemResolver;

  public RoutingResolver(Resolver defaultSystemResolver) {
    this.defaultSystemResolver = defaultSystemResolver;
  }

  public void setRoutingTable(RoutingTable routingTable) {
    safeAccessRoutingTable.set(routingTable);
  }

  public RoutingTable getRoutingTable() {
    return safeAccessRoutingTable.get();
  }

  @Override
  public Message send(Message query) throws IOException {
    Resolver resolverToUse = null;

    final RoutingTable routingTable = safeAccessRoutingTable.get();
    if (routingTable != null) {
      Name name = query.getQuestion().getName();
      resolverToUse = routingTable.getResolverFor(name.toString());
    }

    if (resolverToUse == null)
      resolverToUse = defaultSystemResolver;

    return resolverToUse.send(query);
  }
}
