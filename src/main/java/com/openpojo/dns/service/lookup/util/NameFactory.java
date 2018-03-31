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

package com.openpojo.dns.service.lookup.util;

import java.net.InetAddress;
import java.util.Arrays;

import com.openpojo.dns.exception.ResolveException;
import org.xbill.DNS.Name;
import org.xbill.DNS.ReverseMap;

public class NameFactory {

  public static Name getName(String hostName) {
    try {
      return new Name(hostName);
    } catch (Exception e) {
      throw ResolveException.getInstance("Failed to parse name [" + hostName + "]", e);
    }
  }

  public static Name getName(byte[] addr) {
    try {
      return ReverseMap.fromAddress(InetAddress.getByAddress(addr));
    } catch (Exception e) {
      throw ResolveException.getInstance("Failed to parse address [" + Arrays.toString(addr) + "]", e);
    }
  }

  public static Name getName(InetAddress address) {
    return getName(address.getAddress());
  }

  private NameFactory() { }
}
