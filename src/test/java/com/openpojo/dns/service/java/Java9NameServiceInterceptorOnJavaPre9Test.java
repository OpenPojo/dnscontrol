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

package com.openpojo.dns.service.java;

import com.openpojo.dns.service.java.v9.Java9NameServiceInterceptor;
import com.openpojo.reflection.java.version.Version;
import com.openpojo.reflection.java.version.VersionFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeThat;

/**
 * @author oshoukry
 */
public class Java9NameServiceInterceptorOnJavaPre9Test {
  private String shouldRunOnJavaVersionLessThan = "9";
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    Version runningOn = VersionFactory.getVersion(System.getProperty("java.version"));
    assumeThat(runningOn.compareTo(VersionFactory.getVersion(shouldRunOnJavaVersionLessThan)), is(-1));
  }

  @Test
  public void shouldThrowUnknownExceptionClass() throws ClassNotFoundException {
    thrown.expect(ClassNotFoundException.class);
    thrown.expectMessage("failed to load class [" + Java9NameServiceInterceptor.NAME_SERVICE_CLASS_NAME + "]");
    Java9NameServiceInterceptor java9NameServiceInterceptor = new Java9NameServiceInterceptor();
    java9NameServiceInterceptor.changeInetAddressProxy(java9NameServiceInterceptor.createProxyForNameService());
  }
}
