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

package com.openpojo.dns.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.filters.FilterChain;
import com.openpojo.reflection.filters.FilterClassName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static com.openpojo.reflection.impl.PojoClassFactory.getPojoClassesRecursively;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

/**
 * Runs every test class in the project in a random order, several times, to catch tests that only pass because of
 * state left behind by another test. Uses the JUnit Platform launcher so Jupiter and vintage tests are both covered.
 */
class OutOfOrderTestFragilityTest {
  private static final int ROUNDS = 5;

  private List<PojoClass> testClasses;
  private List<String> orderOfTests;

  @BeforeEach
  void setup() {
    PojoClassFilter testClassFilter = new FilterClassName(".*Test$");
    PojoClassFilter selfFilter = pojoClass -> !pojoClass.getName().equals(OutOfOrderTestFragilityTest.class.getName());
    testClasses = getPojoClassesRecursively("com.openpojo.dns", new FilterChain(testClassFilter, selfFilter));
    orderOfTests = new ArrayList<>();
  }

  @Test
  void regressTestingOrder() {
    for (int i = 0; i < ROUNDS; i++)
      runAllTestsInRandomOrder();
  }

  private void runAllTestsInRandomOrder() {
    Collections.shuffle(testClasses);
    Launcher launcher = LauncherFactory.create();

    boolean passed = true;
    for (PojoClass clazz : testClasses) {
      TestExecutionSummary summary = run(launcher, clazz.getClazz());
      boolean wasSuccessful = summary.getTotalFailureCount() == 0;
      passed &= wasSuccessful;
      String testOutcome = "Test: " + clazz.getName() + "..." + (wasSuccessful ? "Passed!" : "Failed!");
      if (!wasSuccessful)
        for (TestExecutionSummary.Failure failure : summary.getFailures())
          testOutcome += "\n    " + failure.getTestIdentifier().getDisplayName() + ": " + failure.getException();
      orderOfTests.add(testOutcome);
    }

    if (!passed) {
      for (String entry : orderOfTests)
        System.out.println(entry);
      fail("One or more test failed!");
    }
  }

  private static TestExecutionSummary run(Launcher launcher, Class<?> testClass) {
    LauncherDiscoveryRequest request = request().selectors(selectClass(testClass)).build();
    SummaryGeneratingListener listener = new SummaryGeneratingListener();
    launcher.execute(request, listener);
    return listener.getSummary();
  }
}
