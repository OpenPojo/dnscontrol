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

package com.openpojo.dns.testdouble.spy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author oshoukry
 */
class SpyInvocationHandler<T> implements Spy, InvocationHandler {
  private List<String> calls = new ArrayList<>();
  private T instance;

  public SpyInvocationHandler(T instance) {
    this.instance = instance;
  }

  @Override
  public List<String> getCalls() {
    return Collections.unmodifiableList(calls);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getName().equals("getCalls") && args == null)
      return getCalls();

    calls.add(method.getName() + "(" + Arrays.deepToString(args) + ")");
    return method.invoke(instance, args);
  }
}
