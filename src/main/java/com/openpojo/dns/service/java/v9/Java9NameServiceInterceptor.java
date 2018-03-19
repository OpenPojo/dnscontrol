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

package com.openpojo.dns.service.java.v9;

import java.net.InetAddress;

import com.openpojo.dns.service.java.reflection.NameServiceProxy;

import static com.openpojo.dns.service.java.reflection.ReflectionHelper.createProxy;
import static com.openpojo.dns.service.java.reflection.ReflectionHelper.loadClass;
import static com.openpojo.dns.service.java.reflection.ReflectionHelper.setFieldValue;

/**
 * @author oshoukry
 */
public class Java9NameServiceInterceptor {
  public static final String NAME_SERVICE_CLASS_NAME = InetAddress.class.getName() + "$NameService";
  public static final String NAME_SERVICE_FIELD_NAME = "nameService";

  public void changeInetAddressProxy(Object proxyInstance) {
    setFieldValue(InetAddress.class, NAME_SERVICE_FIELD_NAME, null, proxyInstance);
  }

  public Object createProxyForNameService() throws ClassNotFoundException {
    return createProxy(InetAddress.class.getClassLoader(), new NameServiceProxy(), loadClass(NAME_SERVICE_CLASS_NAME));
  }
}
