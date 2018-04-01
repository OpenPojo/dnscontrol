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

package com.openpojo.dns.config.props;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * @author oshoukry
 */
public class FileStreamer {
  public static InputStream getAsStream(String fileName) throws FileNotFoundException {
    return new FileInputStream(getFile(fileName));
  }

  public static boolean exists(String fileName) {
    try {
      return getFile(fileName).exists();
    } catch (Exception ignored) {
      return false;
    }
  }

  public static Date getLastUpdatedDate(String fileName) {
    try {
      return new Date(getFile(fileName).lastModified());
    } catch (Exception ignored) {
      return null;
    }
  }

  private static File getFile(String fileName) {
    final URL resource = getResource(fileName);
    if (resource == null)
      return new File(fileName);
    return new File(resource.getFile());
  }

  private static URL getResource(String fileName) {
    return getClassLoader().getResource(fileName);
  }

  private static ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  private FileStreamer() { }
}
