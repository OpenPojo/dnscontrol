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

package com.openpojo.dns.service.java.v7;

import org.junit.Before;
import org.junit.Test;
import sun.net.spi.nameservice.NameService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

/**
 * @author oshoukry
 */
public class JavaNameServiceDescriptorTest {

  private JavaNameServiceDescriptor javaNameServiceDescriptor;

  @Before
  public void setup() {
    javaNameServiceDescriptor = new JavaNameServiceDescriptor();
  }

  @Test
  public void createNameServiceShouldNotCreateNewEveryTime() {
    NameService nameService = javaNameServiceDescriptor.createNameService();

    assertThat(nameService, notNullValue());
    assertThat(javaNameServiceDescriptor.createNameService(), sameInstance(nameService));
  }

  @Test
  public void validateJavaNameServiceProviderName() {
    assertThat(javaNameServiceDescriptor.getProviderName(), is("dnscontrol"));
  }

  @Test
  public void validateJavaNameServiceType() {
    assertThat(javaNameServiceDescriptor.getType(), is("dns"));
  }
}