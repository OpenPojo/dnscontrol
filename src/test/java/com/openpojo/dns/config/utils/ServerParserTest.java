package com.openpojo.dns.config.utils;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class ServerParserTest {
  @Test
  public void shouldReturnEmptyListWhenNull() {
    final List<String> serversAsList = ServerParser.getServersAsList((String[]) null);
    assertThat(serversAsList, notNullValue());
    assertThat(serversAsList.size(), is(0));
  }

  @SuppressWarnings("RedundantArrayCreation")
  @Test
  public void shouldReturnEmptyListWhenEmptyArrayPassed() {
    final List<String> serversAsList = ServerParser.getServersAsList(new String[0]);
    assertThat(serversAsList, notNullValue());
    assertThat(serversAsList.size(), is(0));
  }

  @Test
  public void shouldReturnArrayForOnlyNonEmptyEntries() {
    String [] entries = {"1", "2", "", "3", null, "4"};
    List<String> expected = Arrays.asList("1", "2", "3", "4");
    final List<String> serversAsList = ServerParser.getServersAsList(entries);
    assertThat(serversAsList, notNullValue());
    assertThat(serversAsList, is(expected));

  }
}
