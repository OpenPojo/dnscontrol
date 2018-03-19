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

import com.openpojo.random.RandomFactory;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.openpojo.dns.service.java.reflection.ReflectionHelper.getFieldByName;
import static com.openpojo.dns.service.java.reflection.ReflectionHelper.loadClass;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class ReflectionHelperTest {

  @SuppressWarnings("unused") // used for field testing bellow by name
  private String someFieldToLoad;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldThrowUnknownClassException() throws ClassNotFoundException {
    final String someNonExistentClassName = "SomeNonExistentClass";
    thrown.expect(ClassNotFoundException.class);
    thrown.expectMessage(someNonExistentClassName);

    loadClass(someNonExistentClassName);
  }

  @Test
  public void canLoadExistingClass() throws ClassNotFoundException {
    assertThat(loadClass(this.getClass().getName()), Matchers.<Class<?>>is(this.getClass()));
  }

  @Test
  public void shouldReturnNullIfFieldNotPresent() {
    assertThat(getFieldByName(this.getClass(), "SomeNonExistentField"), nullValue());
  }

  @Test
  public void shouldReturnFieldIfPresent() {
    assertThat(getFieldByName(this.getClass(), "someFieldToLoad"), notNullValue());
  }

  @Test
  public void shouldReturnFalseIFFailedToSetValue() {
    assertThat(someFieldToLoad, nullValue());
    assertThat(ReflectionHelper.setFieldValue(ReflectionHelperTest.class, "someFieldToLoad", this, new Object()), is(false));
    assertThat(someFieldToLoad, nullValue());
  }

  @Test
  public void shouldSetFieldValueAndReturnTrue() {
    String anyString = RandomFactory.getRandomValue(String.class);
    assertThat(someFieldToLoad, nullValue());
    assertThat(ReflectionHelper.setFieldValue(ReflectionHelperTest.class, "someFieldToLoad", this, anyString), is(true));
    assertThat(someFieldToLoad, is(anyString));

  }

}