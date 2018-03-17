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

import java.util.List;

import com.openpojo.dns.exception.RoutingException;
import com.openpojo.random.RandomFactory;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoMethod;
import com.openpojo.reflection.PojoParameter;
import com.openpojo.reflection.exception.ReflectionException;
import org.junit.Assert;

import static com.openpojo.random.RandomFactory.getRandomValue;
import static com.openpojo.reflection.impl.PojoClassFactory.getPojoClass;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class NotSupportedMethodsValidator {
  public static void validateMethodsNotImplemented(Class<?> clazz, List<String> implementedMethods) {
    PojoClass pojoClass = getPojoClass(clazz);
    Object instance = RandomFactory.getRandomValue(clazz);
    for (PojoMethod method : pojoClass.getPojoMethods()) {
      if (!method.isSynthetic() && !method.isConstructor() && !implementedMethods.contains(method.getName()))
        try {
          method.invoke(instance, getRandomParameters(method));
          Assert.fail("Failed to throw expected exception on method [" + method.getName() + "]");
        } catch (ReflectionException re) {
          RoutingException rre = (RoutingException) re.getCause().getCause();
          assertThat(rre.getMessage(), is("Operation not supported"));
        }
    }
  }

  private static Object[] getRandomParameters(PojoMethod method) {
    final List<PojoParameter> pojoParameters = method.getPojoParameters();
    Object[] parameters = new Object[pojoParameters.size()];
    for (int i = 0; i < parameters.length; i++) {
      if (pojoParameters.get(i).getType().isPrimitive()) {
        parameters[i] = getRandomValue(pojoParameters.get(i));
      } else {
        parameters[i] = null;
      }
    }
    return parameters;
  }
}
