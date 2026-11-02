/*
 * Copyright (c) 2018-2026 Osman Shoukry
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

package org.xbill.DNS;

import java.time.Clock;

/**
 * Test-only door to dnsjava's package-private {@code Cache(Clock, int)}, so TTL expiry can be driven by a test clock
 * instead of sleeps. Lives in dnsjava's package; works on the class path, which is how the tests run.
 */
public final class ClockedCache {

  public static Cache create(Clock clock, int dclass) {
    return new Cache(clock, dclass);
  }

  private ClockedCache() { }
}
