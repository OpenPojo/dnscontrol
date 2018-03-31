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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author oshoukry
 */
public class ReflectionHelper {

  public static Class<?> loadClass(String className) throws ClassNotFoundException {
    try {
      return Class.forName(className);
    } catch (Throwable t) {
      throw new ClassNotFoundException("failed to load class [" + className + "]", t);
    }
  }

  @SuppressWarnings("WeakerAccess")
  public static Field getFieldByName(Class<?> clazz, String fieldName) {
    try {
      final Field declaredField = clazz.getDeclaredField(fieldName);
      declaredField.setAccessible(true);
      return declaredField;
    } catch (Exception ignored) { }
    return null;
  }

  public static Object getFieldValue(Class<?> clazz, String fieldName, Object instance) {
    Object fieldValue = null;
    try {
      Field field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      fieldValue = field.get(instance);
    } catch (Exception ignored) { }
    return fieldValue;
  }

  public static Object invokeMethodOnClass(Class<?> clazz, String methodName, Object instance) {
    try {
      Class<?> classToUse = clazz;
      if (instance != null)
        classToUse = instance.getClass();

      Method method;
      method = classToUse.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return method.invoke(instance);
    } catch (Exception ignored) { }
    return null;
  }

  @SuppressWarnings("ConstantConditions")
  public static <T> boolean setFieldValue(Class<T> clazz, String fieldName, T instance, Object fieldValue) {
    Field field = getFieldByName(clazz, fieldName);
    try {
      field.set(instance, fieldValue);
      return true;
    } catch (Exception ignored) { }
    return false;
  }

  public static Object createProxy(ClassLoader classLoader, InvocationHandler invocationHandler, Class... interfaces) {
    return Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
  }

  private ReflectionHelper() { }
}
