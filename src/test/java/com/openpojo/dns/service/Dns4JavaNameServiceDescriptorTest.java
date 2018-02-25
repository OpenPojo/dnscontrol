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

package com.openpojo.dns.service;

import org.junit.Before;
import org.junit.Test;
import sun.net.spi.nameservice.NameService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class Dns4JavaNameServiceDescriptorTest {

  private Dns4JavaNameServiceDescriptor dns4JavaNameServiceDescriptor;

  @Before
  public void setup() {
    dns4JavaNameServiceDescriptor = new Dns4JavaNameServiceDescriptor();
  }

  @Test
  public void createNameServiceShouldNotCreateNewEveryTime() {
    NameService nameService = dns4JavaNameServiceDescriptor.createNameService();

    assertThat(nameService, notNullValue());
    assertThat(dns4JavaNameServiceDescriptor.createNameService(), sameInstance(nameService));
  }

  @Test
  public void getProviderNameShouldBedns4java() {
    assertThat(dns4JavaNameServiceDescriptor.getProviderName(), is("dns4java"));
  }

  @Test
  public void getTypeShouldReturndns() {
    assertThat(dns4JavaNameServiceDescriptor.getType(), is("dns"));
  }
}