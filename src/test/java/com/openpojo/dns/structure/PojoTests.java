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
import com.openpojo.reflection.filters.FilterEnum;
import com.openpojo.reflection.filters.FilterNonConcrete;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * @author oshoukry
 */
public class PojoTests {

  @Test
  public void testAllGetters() {
    Validator validator = ValidatorBuilder.create()
        .with(new GetterTester())
        .build();
    final List<PojoClass> validated = validator.validateRecursively("com.openpojo.dns", new FilterNonConcrete(), new FilterEnum());
    assertThat(validated.size(), greaterThan(1));
  }
}
