package com.openpojo.dns.exception;

/**
 * @author oshoukry
 */
public class ConfigurationFileNotFoundException extends RuntimeException {
  private ConfigurationFileNotFoundException(String message) {
    super(message);
  }

  public static ConfigurationFileNotFoundException getInstance(String message) {
    return new ConfigurationFileNotFoundException(message);
  }
}
