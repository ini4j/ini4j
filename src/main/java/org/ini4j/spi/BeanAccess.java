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

/**
 * A view of a store of named, multi-valued properties, as a bean needs it.
 *
 * <p>{@link BeanTool} uses it to copy values between a bean and a store such as an {@link
 * org.ini4j.OptionMap}. The names are the names of bean properties, and each can have several
 * values, indexed from zero. {@link org.ini4j.OptionMap#as(Class)} and {@link
 * org.ini4j.OptionMap#from(Object)} are built on it. In the implementation of {@link
 * org.ini4j.BasicOptionMap}, the property name is turned into an option name with the prefix and
 * the upper case rule, and reading resolves variables.
 *
 * @since 0.4.1
 */
public interface BeanAccess {
  /**
   * Adds a value after the existing values of a property.
   *
   * @param propertyName the name of the property
   * @param value the value
   */
  void propAdd(String propertyName, String value);

  /**
   * Removes all the values of a property.
   *
   * @param propertyName the name of the property
   * @return a removed value; {@link org.ini4j.BasicOptionMap} returns the first one, or {@code
   *     null} if there was none
   */
  String propDel(String propertyName);

  /**
   * Returns the last value of a property.
   *
   * @param propertyName the name of the property
   * @return the value, or {@code null} if there is none
   */
  String propGet(String propertyName);

  /**
   * Returns a value of a property.
   *
   * @param propertyName the name of the property
   * @param index the position of the value
   * @return the value, or {@code null} if the property has no value
   */
  String propGet(String propertyName, int index);

  /**
   * Returns the number of values of a property.
   *
   * @param propertyName the name of the property
   * @return the number of values, {@code 0} if there is none
   */
  int propLength(String propertyName);

  /**
   * Replaces the last value of a property, or adds it if there is none.
   *
   * @param propertyName the name of the property
   * @param value the new value
   * @return the replaced value, or {@code null} if there was none
   */
  String propSet(String propertyName, String value);

  /**
   * Replaces a value of a property.
   *
   * @param propertyName the name of the property
   * @param value the new value
   * @param index the position of the value to replace
   * @return the replaced value
   */
  String propSet(String propertyName, String value, int index);
}
