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

import java.util.Map;

/**
 * A {@link Map} that can keep a comment for each key.
 *
 * <p>A comment belongs to a key, not to one of its values. A key can have no comment, which is
 * reported as {@code null}. When {@link Config#isComment()} is on, loading a file stores the
 * comment lines in front of an option or a section as the comment of that option or section, with
 * two exceptions: only the comment directly in front of the entry is kept, and the comment at the
 * top of the file becomes the comment of the whole {@link Profile} or {@link Options} object when
 * {@link Config#isHeaderComment()} is on (see {@link org.ini4j.spi.IniBuilder}). The leading
 * comment character of each line is removed, and the lines are joined with the line separator of
 * the {@link Config}. Storing a file writes the comments back.
 *
 * <p>In {@link CommonMultiMap}, removing a key with {@code remove(Object)}, removing its last value
 * with {@code remove(Object, int)}, and {@code clear()} also remove the comment. Removing a key in
 * another way, for example through {@code keySet()}, does not.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 * @see CommonMultiMap
 * @since 0.5.0
 */
public interface CommentedMap<K, V> extends Map<K, V> {
  /**
   * Returns the comment of a key.
   *
   * @param key the key
   * @return the comment, or {@code null} if the key has no comment
   */
  String getComment(Object key);

  /**
   * Sets the comment of a key.
   *
   * <p>The key does not have to be in the map. {@link CommonMultiMap} stores the comment anyway.
   *
   * @param key the key
   * @param comment the new comment
   * @return the previous comment of the key, or {@code null} if it had none
   */
  String putComment(K key, String comment);

  /**
   * Removes the comment of a key.
   *
   * @param key the key
   * @return the removed comment, or {@code null} if the key had none
   */
  String removeComment(Object key);
}
