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
package org.ini4j;

import java.util.List;
import java.util.Map;

/**
 * A {@link Map} that can hold more than one value for a key.
 *
 * <p>The values of a key are kept in insertion order and are addressed by a zero-based index. The
 * methods inherited from {@link Map} work on the whole key: {@link #get(Object)} returns the last
 * value, {@link #put(Object, Object)} replaces the last value (or adds the first one) and {@link
 * #remove(Object)} removes every value of the key. The methods declared here give access to the
 * other values.
 *
 * <p>A {@link Profile} is a multi-map of sections and an {@link OptionMap} is a multi-map of
 * options, so a profile can hold several sections with the same name and a section can hold several
 * options with the same name. {@link Config#isMultiSection()} and {@link Config#isMultiOption()}
 * decide whether loading a file keeps such repeats. For example, if a section has the option {@code
 * fortuneNumber} three times, {@code section.get("fortuneNumber", 1)} returns the second one and
 * {@code section.length("fortuneNumber")} returns {@code 3}.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 * @see BasicMultiMap
 * @since 0.4.0
 */
public interface MultiMap<K, V> extends Map<K, V> {
  /**
   * Returns all the values of a key.
   *
   * <p>{@link BasicMultiMap} returns the list it stores, not a copy, so changes to the list change
   * the map.
   *
   * @param key the key
   * @return the values of the key in insertion order, or {@code null} if the key is not in the map
   */
  List<V> getAll(Object key);

  /**
   * Adds a value after the existing values of a key.
   *
   * @param key the key; it is added to the map if it is not there yet
   * @param value the value to add
   */
  void add(K key, V value);

  /**
   * Inserts a value at the given position in the values of a key.
   *
   * <p>The values at that position and after it move one place up.
   *
   * @param key the key; it is added to the map if it is not there yet
   * @param value the value to insert
   * @param index the position of the new value
   * @throws IndexOutOfBoundsException if the index is negative or greater than the number of values
   *     the key has
   */
  void add(K key, V value, int index);

  /**
   * Returns the value of a key at the given position.
   *
   * @param key the key
   * @param index the position of the value
   * @return the value, or {@code null} if the key is not in the map
   * @throws IndexOutOfBoundsException if the key is in the map and the index is out of range
   */
  V get(Object key, int index);

  /**
   * Returns the number of values a key has.
   *
   * @param key the key
   * @return the number of values, or {@code 0} if the key is not in the map
   */
  int length(Object key);

  /**
   * Replaces the value of a key at the given position.
   *
   * @param key the key; it must be in the map
   * @param value the new value
   * @param index the position of the value to replace
   * @return the value that was replaced
   * @throws IndexOutOfBoundsException if the index is out of range
   * @throws NullPointerException if the key is not in the map (this is what {@link BasicMultiMap}
   *     does)
   */
  V put(K key, V value, int index);

  /**
   * Replaces all the values of a key with a copy of the given values.
   *
   * @param key the key
   * @param values the new values; {@link BasicMultiMap} copies the list, so later changes to it do
   *     not affect the map
   * @return the list of the values the key had before, or {@code null} if the key was not in the
   *     map
   */
  List<V> putAll(K key, List<V> values);

  /**
   * Removes the value of a key at the given position.
   *
   * <p>When the last value of a key is removed, the key is removed from the map too.
   *
   * @param key the key
   * @param index the position of the value to remove
   * @return the removed value, or {@code null} if the key is not in the map
   * @throws IndexOutOfBoundsException if the key is in the map and the index is out of range
   */
  V remove(Object key, int index);
}
