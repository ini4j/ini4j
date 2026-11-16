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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ini4j.spi.Warnings;

/**
 * A {@link MultiMap} that keeps the values of each key in a list.
 *
 * <p>The map stores a {@link java.util.List} for each key in an underlying map, which is a {@link
 * java.util.LinkedHashMap} unless another one is given to the constructor. Keys therefore come in
 * insertion order by default. The values of a key are in insertion order too. A value can be {@code
 * null}.
 *
 * <p>The inherited {@link java.util.Map} methods work on the whole key as described in {@link
 * MultiMap}. The differences from a plain map are these: {@link #size()} counts keys, not values;
 * {@link #values()} returns all the values of all the keys, so it can have more elements than
 * {@link #size()}; {@link #entrySet()} has one entry for each key, and the entry reads and replaces
 * the last value. The class does not override {@code equals} and {@code hashCode}, so two maps are
 * equal only if they are the same object.
 *
 * <p><strong>Thread safety.</strong> The class has no synchronization. Several threads can read a
 * map built on the default underlying map while no thread changes it. A map built on a collection
 * that changes on reads, such as an access-ordered {@link java.util.LinkedHashMap}, is not safe to
 * read from several threads. Any change needs outside locking.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 * @see CommonMultiMap
 * @since 0.5.0
 */
public class BasicMultiMap<K, V> implements MultiMap<K, V>, Serializable {
  private static final long serialVersionUID = 4716749660560043989L;
  private final Map<K, List<V>> _impl;

  /** Creates an empty map that keeps its keys in insertion order. */
  public BasicMultiMap() {
    this(new LinkedHashMap<K, List<V>>());
  }

  /**
   * Creates a map on top of an underlying map.
   *
   * <p>The given map is used directly, not copied, so changes to it show in this map and the other
   * way round. It must map each key to a modifiable list. The order of the keys is the iteration
   * order of the given map.
   *
   * @param impl the underlying map, used as it is
   */
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP2")
  public BasicMultiMap(Map<K, List<V>> impl) {
    _impl = impl;
  }

  @Override
  public List<V> getAll(Object key) {
    return _impl.get(key);
  }

  @Override
  public boolean isEmpty() {
    return _impl.isEmpty();
  }

  @Override
  public void add(K key, V value) {
    getList(key, true).add(value);
  }

  @Override
  public void add(K key, V value, int index) {
    getList(key, true).add(index, value);
  }

  @Override
  public void clear() {
    _impl.clear();
  }

  @Override
  public boolean containsKey(Object key) {
    return _impl.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    boolean ret = false;

    for (List<V> all : _impl.values()) {
      if (all.contains(value)) {
        ret = true;

        break;
      }
    }

    return ret;
  }

  /**
   * Returns the entries of this map, one for each key.
   *
   * <p>The set is a new one and is not backed by the map: removing from it does not change the map,
   * and it is not ordered. The value of an entry is the last value of its key, and {@link
   * java.util.Map.Entry#setValue(Object)} replaces that last value.
   *
   * @return a new set of entries
   */
  @Override
  public Set<Entry<K, V>> entrySet() {
    Set<Entry<K, V>> ret = new HashSet<Entry<K, V>>();

    for (K key : keySet()) {
      ret.add(new ShadowEntry(key));
    }

    return ret;
  }

  @Override
  public V get(Object key) {
    List<V> values = getList(key, false);

    return (values == null) ? null : values.get(values.size() - 1);
  }

  @Override
  public V get(Object key, int index) {
    List<V> values = getList(key, false);

    return (values == null) ? null : values.get(index);
  }

  /**
   * Returns the keys of the underlying map.
   *
   * <p>The set is a live view of the underlying map, so removing a key from it removes all the
   * values of the key.
   *
   * @return the set of keys
   */
  @Override
  public Set<K> keySet() {
    return _impl.keySet();
  }

  @Override
  public int length(Object key) {
    List<V> values = getList(key, false);

    return (values == null) ? 0 : values.size();
  }

  @Override
  public V put(K key, V value) {
    V ret = null;
    List<V> values = getList(key, true);

    if (values.isEmpty()) {
      values.add(value);
    } else {
      ret = values.set(values.size() - 1, value);
    }

    return ret;
  }

  @Override
  public V put(K key, V value, int index) {
    return getList(key, false).set(index, value);
  }

  /**
   * Copies the entries of another map.
   *
   * <p>If the argument is a {@link MultiMap}, all the values of each of its keys replace the values
   * of that key in this map (as {@link #putAll(Object, List)} does). Otherwise each entry replaces
   * the last value of its key.
   *
   * @param map the map to copy from
   */
  @SuppressWarnings(Warnings.UNCHECKED)
  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    if (map instanceof MultiMap) {
      MultiMap<K, V> mm = (MultiMap<K, V>) map;

      for (Object key : mm.keySet()) {
        putAll((K) key, mm.getAll(key));
      }
    } else {
      for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
        put(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Replaces all the values of a key with a copy of the given list.
   *
   * <p>Do not pass an empty list. The key would then be in the map with no value, and {@link
   * #get(Object)} for it would throw an {@link IndexOutOfBoundsException}.
   *
   * @param key the key
   * @param values the new values, copied
   * @return the list of values the key had before, as the map stored it, or {@code null} if the key
   *     was not in the map
   */
  @Override
  public List<V> putAll(K key, List<V> values) {
    List<V> ret = _impl.get(key);

    _impl.put(key, new ArrayList<V>(values));

    return ret;
  }

  /**
   * Removes all the values of a key.
   *
   * @param key the key
   * @return the first value the key had, or {@code null} if the key was not in the map
   */
  @Override
  public V remove(Object key) {
    List<V> prev = _impl.remove(key);

    return (prev == null) ? null : prev.get(0);
  }

  @Override
  public V remove(Object key, int index) {
    V ret = null;
    List<V> values = getList(key, false);

    if (values != null) {
      ret = values.remove(index);
      if (values.isEmpty()) {
        _impl.remove(key);
      }
    }

    return ret;
  }

  /**
   * Returns the number of keys.
   *
   * @return the number of keys, not the number of values
   */
  @Override
  public int size() {
    return _impl.size();
  }

  /**
   * Returns the text form of the underlying map, for example {@code {a=[1, 2], b=[3]}}.
   *
   * @return the text form
   */
  @Override
  public String toString() {
    return _impl.toString();
  }

  /**
   * Returns all the values of all the keys.
   *
   * <p>The collection is a new list, in key order and, for each key, in value order. Changes to it
   * do not change the map.
   *
   * @return a new collection of values
   */
  @Override
  public Collection<V> values() {
    List<V> all = new ArrayList<V>(_impl.size());

    for (List<V> values : _impl.values()) {
      all.addAll(values);
    }

    return all;
  }

  @SuppressWarnings(Warnings.UNCHECKED)
  private List<V> getList(Object key, boolean create) {
    List<V> values = _impl.get(key);

    if ((values == null) && create) {
      values = new ArrayList<V>();
      _impl.put((K) key, values);
    }

    return values;
  }

  class ShadowEntry implements Map.Entry<K, V> {
    private final K _key;

    ShadowEntry(K key) {
      _key = key;
    }

    @Override
    public K getKey() {
      return _key;
    }

    @Override
    public V getValue() {
      return get(_key);
    }

    @Override
    public V setValue(V value) {
      return put(_key, value);
    }
  }
}
