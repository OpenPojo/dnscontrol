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

package com.openpojo.dns.cache.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoField;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;

import static com.openpojo.dns.cache.CacheControl.D_CLASS_TYPES;
import static com.openpojo.reflection.impl.PojoClassFactory.getPojoClass;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author oshoukry
 */
public class VerificationHelper {

  public static void verifyCacheIsEmpty() {
    for (int i = 0; i < D_CLASS_TYPES.length; i++)
      assertThat(Lookup.getDefaultCache(D_CLASS_TYPES[i]).getSize(), is(0));
  }

  public static int[] getDClassTypesSorted() {
    Set<Integer> cacheTypes = new HashSet<>();
    PojoClass dclassPojoClass = getPojoClass(DClass.class);
    for (PojoField field : dclassPojoClass.getPojoFields())
      if (isPublicStaticFinalInt(field)) {
        cacheTypes.add((int) field.get(null));
      }
    int[] array = new int[cacheTypes.size()];
    final Integer[] integers = cacheTypes.toArray(new Integer[0]);
    for (int i = 0; i < array.length; i++) {
      array[i] = integers[i];
    }

    Arrays.sort(array);
    return array;
  }

  private static boolean isPublicStaticFinalInt(PojoField field) {
    return field.isPublic() && field.isStatic() && field.isFinal() && field.getType() == int.class;
  }
}
