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

import java.util.List;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.PojoField;
import com.openpojo.reflection.filters.FilterEnum;
import com.openpojo.reflection.filters.FilterNonConcrete;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class PojoValidationTest {

  private static final String TOP_LEVEL_PACKAGE = "com.openpojo.dns";

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testAllGetters() {
    Validator validator = ValidatorBuilder.create()
        .with(new GetterTester())
        .build();

    PojoClassFilter[] pojoClassFilters = new PojoClassFilter[] {
        new FilterNonConcrete(),
        new FilterEnum(),
        new FilterTestClasses(),
        new FilterHasGetter()
    };

    final List<PojoClass> validated = validator.validateRecursively(TOP_LEVEL_PACKAGE, pojoClassFilters);
    assertThat(validated.size(), greaterThan(1));
  }

  @Test
  public void testAllSetters() {
    Validator validator = ValidatorBuilder.create()
        .with(new SetterTester())
        .build();

    PojoClassFilter[] pojoClassFilters = new PojoClassFilter[] {
        new FilterNonConcrete(),
        new FilterEnum(),
        new FilterTestClasses(),
        new FilterHasSetter() };
    final List<PojoClass> validated = validator.validateRecursively(TOP_LEVEL_PACKAGE, pojoClassFilters);

    assertThat(validated.size(), is(0));
  }

  private static class FilterTestClasses implements PojoClassFilter {
    @Override
    public boolean include(PojoClass pojoClass) {
      return !pojoClass.getSourcePath().contains("target/test-classes/");
    }
  }

  private static class FilterHasSetter implements PojoClassFilter {
    @Override
    public boolean include(PojoClass pojoClass) {
      for (PojoField field : pojoClass.getPojoFields()) {
        if (field.hasSetter()) {
          return true;
        }
      }
      return false;
    }
  }

  private static class FilterHasGetter implements PojoClassFilter {
    @Override
    public boolean include(PojoClass pojoClass) {
      for (PojoField field : pojoClass.getPojoFields()) {
        if (field.hasGetter()) {
          return true;
        }
      }
      return false;
    }
  }
}
