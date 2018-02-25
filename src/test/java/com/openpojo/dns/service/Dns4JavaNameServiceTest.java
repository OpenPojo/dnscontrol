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

import com.openpojo.dns.service.initialize.DefaultDomain;
import com.openpojo.dns.service.initialize.DefaultIPv6Preference;
import com.openpojo.dns.service.initialize.DefaultResolver;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author oshoukry
 */
public class Dns4JavaNameServiceTest {

  private Dns4JavaNameService nameService;

  @Test
  public void shouldInitInitializers() {
    final DefaultDomainSpy defaultDomain = new DefaultDomainSpy();
    final DefaultIPv6PreferenceSpy ipV6Preference = new DefaultIPv6PreferenceSpy();
    final DefaultResolverSpy resolver = new DefaultResolverSpy();

    nameService = new Dns4JavaNameService(defaultDomain, ipV6Preference, resolver);

    Assert.assertThat(defaultDomain.initCalled, is(true));
    Assert.assertThat(ipV6Preference.initCalled, is(true));
    Assert.assertThat(resolver.initCalled, is(true));
  }

  private static class DefaultDomainSpy extends DefaultDomain {
    private boolean initCalled = false;
    @Override
    public void init() {
      initCalled = true;
    }
  }

  private static class DefaultIPv6PreferenceSpy extends DefaultIPv6Preference {
    private boolean initCalled = false;
    @Override
    public void init() {
      initCalled = true;
    }
  }

  private static class DefaultResolverSpy extends DefaultResolver {
    private boolean initCalled = false;
    @Override
    public void init() {
      initCalled = true;
    }
  }
}