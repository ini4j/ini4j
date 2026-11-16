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

/**
 * A map of named options, where each option can have several values.
 *
 * <p>An option map is the content of a section of a {@link Profile}, or of an {@link Options} file.
 * Names and values are strings. The values of one name are indexed as in {@link MultiMap}, and
 * every name can also have a comment as in {@link CommentedMap}. A stored value can be {@code
 * null}, for example for an option that has a name but no value in the file.
 *
 * <p>On top of the string access this interface offers methods that convert values, resolve
 * variables and work with beans.
 *
 * <p>The {@code get} and {@code getAll} methods with a {@link Class} argument convert the stored
 * text to another type. Supported types are the primitive types and their wrappers, {@link String},
 * {@link Character}, {@link java.io.File}, {@link java.net.URL}, {@link java.net.URI}, {@link
 * Class}, {@link java.util.TimeZone}, and any class with a static {@code valueOf(String)} method. A
 * conversion that fails throws an {@link IllegalArgumentException}, except that converting an empty
 * value to {@link Character} throws a {@link StringIndexOutOfBoundsException}. A missing name or a
 * {@code null} value converts to the zero value of a primitive type ({@code 0}, {@code false} or
 * {@code '\0'}) and to {@code null} for any other type. The {@code put} and {@code add} methods
 * that take an {@link Object} store {@link String#valueOf(Object)} of the value, and keep {@code
 * null} as {@code null}.
 *
 * <p>The {@code fetch} methods work like the {@code get} methods, but they first replace variables
 * in the value. A variable is written as {@code ${name}} for the last value of the option {@code
 * name} in the same map, or {@code ${name[2]}} for the value at index 2. A variable written as
 * {@code ${@env/NAME}} reads the environment variable {@code NAME} and {@code ${@prop/name}} reads
 * the system property {@code name}. A variable with a backslash in front of the dollar sign is not
 * resolved. A variable that refers to a missing option, environment variable or property is left in
 * the text unchanged. A value can contain variables that contain variables, up to the limit of
 * {@link Config#getMaxResolveDepth()}; deeper nesting, such as an option that refers to itself,
 * throws a {@link CircularReferenceException}. In a section of a {@link Profile} a variable can
 * also name an option of another section; see {@link Profile#fetch(Object, Object)}.
 *
 * <p>{@link #as(Class)} gives a typed view of the map through an interface, and {@link
 * #from(Object)} and {@link #to(Object)} copy the values between the map and a bean. The option
 * name of a bean property is the property name, for example {@code homeDir} for {@code
 * getHomeDir()}, with the first letter in upper case if the map was created that way (see {@link
 * Config#isPropertyFirstUpper()}).
 *
 * @see Profile.Section
 * @see Options
 * @since 0.4.1
 */
public interface OptionMap extends MultiMap<String, String>, CommentedMap<String, String> {
  /**
   * Returns all the values of an option, converted to the component type of an array class.
   *
   * <p>The values are returned as stored, without variable substitution. Use {@link
   * #fetchAll(Object, Class)} to resolve variables.
   *
   * @param <T> the array type
   * @param key the option name
   * @param clazz the array class, for example {@code int[].class}
   * @return a new array with one element for each value; it is empty if the option is missing
   * @throws IllegalArgumentException if {@code clazz} is not an array class, or if a value cannot
   *     be converted
   */
  <T> T getAll(Object key, Class<T> clazz);

  /**
   * Adds a value after the existing values of an option.
   *
   * @param key the option name
   * @param value the value; it is stored as {@link String#valueOf(Object)}, or as {@code null} if
   *     it is {@code null}
   */
  void add(String key, Object value);

  /**
   * Inserts a value at the given position in the values of an option.
   *
   * @param key the option name
   * @param value the value; it is stored as {@link String#valueOf(Object)}, or as {@code null} if
   *     it is {@code null}
   * @param index the position of the new value
   * @throws IndexOutOfBoundsException if the index is negative or greater than the number of values
   *     the option has
   */
  void add(String key, Object value, int index);

  /**
   * Returns a view of this map as an object of the given interface.
   *
   * <p>The returned object is a dynamic proxy. It is backed by this map, so it always shows the
   * current content and its setters change the map. A getter such as {@code getAge()} reads the
   * option {@code age} as {@link #fetch(Object)} does (variables are resolved) and converts the
   * value to the return type. A getter returns the zero value of its type if the option is missing
   * or the value cannot be converted. A setter such as {@code setAge(int)} replaces the last value
   * of the option with the given value. A {@code hasAge()} method tells whether the option exists.
   * For an array property, the getter reads all the values and the setter replaces all of them. A
   * setter does not accept {@code null}: it throws a {@link NullPointerException} for a {@code
   * null} value, array or array element, and an array setter has already removed the existing
   * values by then. Interfaces can also declare the {@code addPropertyChangeListener} and {@code
   * addVetoableChangeListener} methods (and the matching {@code remove} methods), which take the
   * property name and the listener. The proxy does not implement the methods of {@link Object}:
   * {@code toString()} returns {@code null}, and {@code equals} and {@code hashCode} throw an
   * exception.
   *
   * @param <T> the interface type
   * @param clazz the interface to implement
   * @return the proxy
   * @throws IllegalArgumentException if {@code clazz} is not an interface
   */
  <T> T as(Class<T> clazz);

  /**
   * Returns a view of this map as an object of the given interface, with a prefix on the option
   * names.
   *
   * <p>It works like {@link #as(Class)}, except that the option for a property is named by the
   * prefix followed by the property name. With the prefix {@code "dwarf."}, the getter {@code
   * getAge()} reads the option {@code dwarf.age}.
   *
   * @param <T> the interface type
   * @param clazz the interface to implement
   * @param keyPrefix the text put in front of every property name
   * @return the proxy
   * @throws IllegalArgumentException if {@code clazz} is not an interface
   */
  <T> T as(Class<T> clazz, String keyPrefix);

  /**
   * Returns the last value of an option, with variables resolved.
   *
   * <p>See the description of this interface for the variable syntax.
   *
   * @param key the option name
   * @return the resolved value, or {@code null} if the option is missing or its value is {@code
   *     null}
   * @throws CircularReferenceException if the variables are nested deeper than {@link
   *     Config#getMaxResolveDepth()}
   */
  String fetch(Object key);

  /**
   * Returns the last value of an option, or a default if the option is missing.
   *
   * <p>In {@link BasicOptionMap} the value is returned as stored, the same as {@link #get(Object,
   * String)}: variables are not resolved.
   *
   * @param key the option name
   * @param defaultValue the value to return when there is no value
   * @return the last value, or {@code defaultValue} if there is none
   */
  String fetch(Object key, String defaultValue);

  /**
   * Returns the value of an option at the given position, with variables resolved.
   *
   * @param key the option name
   * @param index the position of the value
   * @return the resolved value, or {@code null} if the option is missing
   * @throws IndexOutOfBoundsException if the option exists and the index is out of range
   * @throws CircularReferenceException if the variables are nested deeper than {@link
   *     Config#getMaxResolveDepth()}
   */
  String fetch(Object key, int index);

  /**
   * Returns the last value of an option, with variables resolved, converted to a type.
   *
   * @param <T> the target type
   * @param key the option name
   * @param clazz the target type
   * @return the converted value; the zero value of a primitive type, or {@code null}, if the option
   *     is missing
   * @throws IllegalArgumentException if the value cannot be converted
   * @throws CircularReferenceException if the variables are nested deeper than {@link
   *     Config#getMaxResolveDepth()}
   */
  <T> T fetch(Object key, Class<T> clazz);

  /**
   * Returns the last value of an option, with variables resolved, converted to a type, or a default
   * if the option is missing.
   *
   * @param <T> the target type
   * @param key the option name
   * @param clazz the target type
   * @param defaultValue the value to return when there is no value
   * @return the converted value, or {@code defaultValue} if there is none
   * @throws IllegalArgumentException if the value cannot be converted
   * @throws CircularReferenceException if the variables are nested deeper than {@link
   *     Config#getMaxResolveDepth()}
   */
  <T> T fetch(Object key, Class<T> clazz, T defaultValue);

  /**
   * Returns the value of an option at the given position, with variables resolved, converted to a
   * type.
   *
   * @param <T> the target type
   * @param key the option name
   * @param index the position of the value
   * @param clazz the target type
   * @return the converted value; the zero value of a primitive type, or {@code null}, if the option
   *     is missing
   * @throws IllegalArgumentException if the value cannot be converted
   * @throws IndexOutOfBoundsException if the option exists and the index is out of range
   * @throws CircularReferenceException if the variables are nested deeper than {@link
   *     Config#getMaxResolveDepth()}
   */
  <T> T fetch(Object key, int index, Class<T> clazz);

  /**
   * Returns all the values of an option, with variables resolved, converted to the component type
   * of an array class.
   *
   * @param <T> the array type
   * @param key the option name
   * @param clazz the array class, for example {@code int[].class}
   * @return a new array with one element for each value; it is empty if the option is missing
   * @throws IllegalArgumentException if {@code clazz} is not an array class, or if a value cannot
   *     be converted
   * @throws CircularReferenceException if the variables are nested deeper than {@link
   *     Config#getMaxResolveDepth()}
   */
  <T> T fetchAll(Object key, Class<T> clazz);

  /**
   * Copies the properties of a bean into this map.
   *
   * <p>Each readable property (except {@code class}) whose value is not {@code null} is written to
   * the option of the same name, as {@link String#valueOf(Object)} of the value. A single value
   * replaces the last value of the option. The elements of an array property are added after the
   * existing values.
   *
   * @param bean the bean to read
   * @throws IllegalArgumentException if a property cannot be read
   */
  void from(Object bean);

  /**
   * Copies the properties of a bean into this map, with a prefix on the option names.
   *
   * <p>It works like {@link #from(Object)}, except that the option for a property is named by the
   * prefix followed by the property name.
   *
   * @param bean the bean to read
   * @param keyPrefix the text put in front of every property name
   * @throws IllegalArgumentException if a property cannot be read
   */
  void from(Object bean, String keyPrefix);

  /**
   * Returns the last value of an option, or a default if the option is missing.
   *
   * <p>The value is returned as stored, without variable substitution.
   *
   * @param key the option name
   * @param defaultValue the value to return when there is no value
   * @return the last value, or {@code defaultValue} if there is none
   */
  String get(Object key, String defaultValue);

  /**
   * Returns the last value of an option, converted to a type.
   *
   * <p>The value is converted as stored, without variable substitution.
   *
   * @param <T> the target type
   * @param key the option name
   * @param clazz the target type
   * @return the converted value; the zero value of a primitive type, or {@code null}, if the option
   *     is missing
   * @throws IllegalArgumentException if the value cannot be converted
   */
  <T> T get(Object key, Class<T> clazz);

  /**
   * Returns the last value of an option, converted to a type, or a default if the option is
   * missing.
   *
   * <p>The value is converted as stored, without variable substitution.
   *
   * @param <T> the target type
   * @param key the option name
   * @param clazz the target type
   * @param defaultValue the value to return when there is no value
   * @return the converted value, or {@code defaultValue} if there is none
   * @throws IllegalArgumentException if the value cannot be converted
   */
  <T> T get(Object key, Class<T> clazz, T defaultValue);

  /**
   * Returns the value of an option at the given position, converted to a type.
   *
   * <p>The value is converted as stored, without variable substitution.
   *
   * @param <T> the target type
   * @param key the option name
   * @param index the position of the value
   * @param clazz the target type
   * @return the converted value; the zero value of a primitive type, or {@code null}, if the option
   *     is missing
   * @throws IllegalArgumentException if the value cannot be converted
   * @throws IndexOutOfBoundsException if the option exists and the index is out of range
   */
  <T> T get(Object key, int index, Class<T> clazz);

  /**
   * Replaces the last value of an option.
   *
   * <p>If the option has no value yet, the value is added.
   *
   * @param key the option name
   * @param value the value; it is stored as {@link String#valueOf(Object)}, or as {@code null} if
   *     it is {@code null}
   * @return the replaced value, or {@code null} if there was none
   */
  String put(String key, Object value);

  /**
   * Replaces the value of an option at the given position.
   *
   * @param key the option name; it must be in the map
   * @param value the value; it is stored as {@link String#valueOf(Object)}, or as {@code null} if
   *     it is {@code null}
   * @param index the position of the value to replace
   * @return the replaced value
   * @throws IndexOutOfBoundsException if the index is out of range
   * @throws NullPointerException if the option is not in the map (this is what {@link
   *     BasicOptionMap} does)
   */
  String put(String key, Object value, int index);

  /**
   * Replaces all the values of an option with the elements of an array.
   *
   * <p>The option is removed first, so its comment is removed too. Each element is stored as {@link
   * String#valueOf(Object)}, or as {@code null} if it is {@code null}. If {@code value} is {@code
   * null}, the option is only removed.
   *
   * @param key the option name
   * @param value an array, of an object or a primitive type, or {@code null}
   * @throws IllegalArgumentException if {@code value} is not an array
   */
  void putAll(String key, Object value);

  /**
   * Copies the values of this map into the properties of a bean.
   *
   * <p>For each writable property of the bean that has an option of the same name, the option value
   * is read as {@link #fetch(Object)} does (variables are resolved), converted to the property type
   * and set. An array property gets all the values. A property without an option is not touched.
   *
   * @param bean the bean to change
   * @throws IllegalArgumentException if a property cannot be set; the message names the property
   */
  void to(Object bean);

  /**
   * Copies the values of this map into the properties of a bean, with a prefix on the option names.
   *
   * <p>It works like {@link #to(Object)}, except that the option for a property is named by the
   * prefix followed by the property name.
   *
   * @param bean the bean to change
   * @param keyPrefix the text put in front of every property name
   * @throws IllegalArgumentException if a property cannot be set; the message names the property
   */
  void to(Object bean, String keyPrefix);
}
