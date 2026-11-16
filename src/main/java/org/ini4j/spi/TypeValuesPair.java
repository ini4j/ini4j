/*
 * Copyright 2005,2009 Ivan SZKIBA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ini4j.spi;

import org.ini4j.Registry.Type;

/**
 * A registry value type with the values of that type.
 *
 * <p>It is the result of {@link RegEscapeTool#decode(String)} and the argument of {@link
 * RegEscapeTool#encode(TypeValuesPair)}. The array of values is not copied, neither by the
 * constructor nor by {@link #getValues()}, so a change to the array shows in the pair. The class
 * has no synchronization.
 *
 * @since 0.5.0
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
public class TypeValuesPair {
  private final Type _type;
  private final String[] _values;

  /**
   * Creates a pair.
   *
   * @param type the type of the values
   * @param values the values; the array is stored, not copied
   */
  @SuppressWarnings("PMD.ArrayIsStoredDirectly")
  public TypeValuesPair(Type type, String[] values) {
    _type = type;
    _values = values;
  }

  /**
   * Returns the type.
   *
   * @return the type
   */
  public Type getType() {
    return _type;
  }

  /**
   * Returns the values.
   *
   * @return the array that was given to the constructor, not a copy
   */
  public String[] getValues() {
    return _values;
  }
}
