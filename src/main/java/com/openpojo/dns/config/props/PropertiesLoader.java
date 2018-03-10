package com.openpojo.dns.config.props;

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
    return getAsStream() != null;
  }

  public String getFileName() {
    return fileName;
  }

  public Map<String, String> getAllProperties() {
    Map<String, String> propertiesMap = new HashMap<>();
    for (String entry : properties.stringPropertyNames())
      propertiesMap.put(entry, properties.getProperty(entry));
    return propertiesMap;
  }

  private InputStream getAsStream() {
    return ClassLoader.getSystemResourceAsStream(fileName);
  }

  public String get(String key) {
    return properties.getProperty(key);
  }

}
