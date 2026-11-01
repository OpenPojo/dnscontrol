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

package com.openpojo.dns.smoke;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Runs in a fresh JVM with dnscontrol on the class path and prints what the JDK resolves a name to, one address per
 * line, or the exception class name. The integration test reads this output.
 */
public final class SmokeMain {
  public static void main(String[] args) {
    try {
      for (InetAddress address : InetAddress.getAllByName(args[0]))
        System.out.println(address.getHostAddress());
    } catch (UnknownHostException e) {
      System.out.println(e.getClass().getName());
    }
  }

  private SmokeMain() { }
}
