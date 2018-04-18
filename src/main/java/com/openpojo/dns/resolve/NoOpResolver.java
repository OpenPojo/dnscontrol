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

package com.openpojo.dns.resolve;

import com.openpojo.dns.exception.ResolveException;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.Message;

/**
 * @author oshoukry
 */
public class NoOpResolver extends UnimplementedResolver {

  @Override
  public Message send(Message query) {
    try {
      final Message answer = (Message) query.clone();
      final Header header = answer.getHeader();
      header.setFlag(Flags.RA);
      header.setFlag(Flags.QR);
      return answer;
    } catch (Exception e) {
      throw ResolveException.getInstance("Failed to resolve for query [" + query + "]", e);
    }
  }
}
