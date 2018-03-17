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

package com.openpojo.dns.issues.issue8;

import java.net.UnknownHostException;

import com.openpojo.dns.service.java.JavaNameService;
import com.openpojo.dns.service.initialize.DefaultDomain;
import com.openpojo.dns.service.initialize.DefaultIPv6Preference;
import com.openpojo.dns.service.initialize.DefaultResolver;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author oshoukry
 */
public class IssueTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldThrowUnknownHostException() throws UnknownHostException {
    thrown.expect(UnknownHostException.class);
    final String localhost = "localhost";
    thrown.expectMessage("Unknown host [" + localhost + "]");
    final JavaNameService javaNameService = new JavaNameService(new DefaultDomain(), new DefaultIPv6Preference(), new DefaultResolver());
    javaNameService.lookupAllHostAddr(localhost);
  }
}
