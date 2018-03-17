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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author oshoukry
 */
public class PropertiesLoader {
  private final String fileName;
  private Properties properties;
  private AtomicBoolean loaded = new AtomicBoolean(false);

  public PropertiesLoader(String fileName) {
    this.fileName = fileName;
  }

  public synchronized void load() {
    if (!loaded.get()) {
      properties = new Properties();
      try {
        properties.load(getAsStream());
      } catch (Exception ignored) {}
    }
    loaded.set(true);
  }

  public boolean exists() {
    return getClassLoader().getResource(fileName) != null || new File(fileName).exists();
  }

  public Map<String, String> getAllProperties() {
    Map<String, String> propertiesMap = new HashMap<>();
    for (String entry : properties.stringPropertyNames())
      propertiesMap.put(entry, properties.getProperty(entry));
    return propertiesMap;
  }

  private InputStream getAsStream() throws IOException {
    final InputStream resourceAsStream = getClassLoader().getResourceAsStream(fileName);
    if (resourceAsStream == null)
      return new FileInputStream(fileName);
    return resourceAsStream;
  }

  private ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  public String get(String key) {
    return properties.getProperty(key);
  }

}
