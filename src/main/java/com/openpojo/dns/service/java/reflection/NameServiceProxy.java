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

package com.openpojo.dns.service.java.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.UnknownHostException;

import com.openpojo.dns.service.lookup.SimpleNameServiceLookup;

/**
 * @author oshoukry
 */
public class NameServiceProxy implements InvocationHandler {
  private final SimpleNameServiceLookup nameServiceLookup = new SimpleNameServiceLookup();

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws UnknownHostException {
    if (method.getName().equals("lookupAllHostAddr"))
      return nameServiceLookup.lookupAllHostAddr(String.class.cast(args[0]));

    if (method.getName().equals("getHostByAddr"))
      return nameServiceLookup.getHostByAddr(byte[].class.cast(args[0]));

    if (method.getName().equals("toString"))
      return nameServiceLookup.toString().replace("@", ".Generated_Proxy@");

    if (method.getName().equals("hashCode"))
      return nameServiceLookup.hashCode();

    if (method.getName().equals("equals"))
      return nameServiceLookup.equals(args[0]);

    throw new UnsupportedOperationException("Invalid method call [" + method.getName() + "] to NameServiceProxy!");
  }

  public NameServiceProxy() {
  }
}
