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
 * A set of named sections, each holding options.
 *
 * <p>A profile is a {@link MultiMap} from section names to {@link Section}s and a {@link
 * CommentedMap}, so a name can have several sections and every section can have a comment. The
 * profile itself can have a header comment. Besides the plain map access, it offers shortcuts that
 * address an option by section name and option name, for example {@link #get(Object, Object)} and
 * {@link #put(String, String, Object)}.
 *
 * <p>Section names can form a tree. The name {@code dwarfs/happy} is the name of a child of the
 * section {@code dwarfs}, and {@code /} is the path separator (see {@link #PATH_SEPARATOR} and
 * {@link Config#getPathSeparator()}). The {@link Section} interface has methods to walk this tree.
 *
 * <p>The example reads two options from a file, adds a section and writes the file back.
 *
 * <pre>{@code
 * Ini ini = new Ini(new File("dwarfs.ini"));
 * int age = ini.get("happy", "age", int.class);
 * String dir = ini.get("happy", "homeDir");
 *
 * ini.put("sleepy", "age", 55);
 * ini.store();
 * }</pre>
 *
 * @see Ini
 * @see Registry
 * @since 0.5.0
 */
public interface Profile
    extends MultiMap<String, Profile.Section>, CommentedMap<String, Profile.Section> {
  /** The default path separator in section names. */
  char PATH_SEPARATOR = '/';

  /**
   * Returns the header comment of the profile.
   *
   * <p>When a file is loaded with {@link Config#isHeaderComment()} and {@link Config#isComment()}
   * on, the comment at the top of the file, before the first section or option, becomes the header
   * comment.
   *
   * @return the comment, or {@code null} if there is none
   */
  String getComment();

  /**
   * Sets the header comment of the profile.
   *
   * @param value the new comment, or {@code null} to have none
   */
  void setComment(String value);

  /**
   * Adds a new, empty section.
   *
   * <p>A section is added even if one with this name exists already. In tree mode (see {@link
   * Config#isTree()}), if the section name has a parent (for example {@code a/b} for {@code a/b/c})
   * and the parent is missing, the parent is added first, as far up as needed.
   *
   * @param sectionName the name of the new section
   * @return the new section
   */
  Section add(String sectionName);

  /**
   * Adds an option value to a section.
   *
   * <p>The section is the last one with the given name; it is created if there is none. The value
   * is added after the values the option has already.
   *
   * @param sectionName the section name
   * @param optionName the option name
   * @param value the value, stored as in {@link OptionMap#add(String, Object)}
   */
  void add(String sectionName, String optionName, Object value);

  /**
   * Returns a view of this profile as an object of the given interface.
   *
   * <p>The returned object is a dynamic proxy backed by this profile. Each property of the
   * interface stands for a section of the same name, and its value is a view of that section
   * created as {@link OptionMap#as(Class)} does. A property of an array type stands for all the
   * sections of that name. A getter returns the zero value of its type if there is no such section.
   * A setter removes the sections of that name and adds new ones filled with {@link
   * OptionMap#from(Object)}; a {@code null} value only removes them. The listener methods work as
   * described in {@link OptionMap#as(Class)}.
   *
   * @param <T> the interface type
   * @param clazz the interface to implement
   * @return the proxy
   * @throws IllegalArgumentException if {@code clazz} is not an interface
   */
  <T> T as(Class<T> clazz);

  /**
   * Returns a view of this profile as an object of the given interface, with a prefix on the
   * section names.
   *
   * <p>It works like {@link #as(Class)}, except that the section for a property is named by the
   * prefix followed by the property name. In {@link BasicProfile}, the prefix is not applied when
   * {@link Config#isPropertyFirstUpper()} is on.
   *
   * @param <T> the interface type
   * @param clazz the interface to implement
   * @param prefix the text put in front of every property name
   * @return the proxy
   * @throws IllegalArgumentException if {@code clazz} is not an interface
   */
  <T> T as(Class<T> clazz, String prefix);

  /**
   * Returns the last value of an option, with variables resolved.
   *
   * <p>The section is the last one with the given name. Variables are written as described in
   * {@link OptionMap}, with these additions:
   *
   * <ul>
   *   <li>{@code ${option}} is an option of the same section.
   *   <li>{@code ${section/option}} is an option of another section. Both parts can carry an index,
   *       as in {@code ${section[1]/option[2]}}. Without an index, the last section and the last
   *       value are used.
   *   <li>{@code ${@env/NAME}} and {@code ${@prop/name}} read an environment variable and a system
   *       property.
   * </ul>
   *
   * @param sectionName the section name
   * @param optionName the option name
   * @return the resolved value, or {@code null} if the section or the option is missing
   * @throws CircularReferenceException if the variables are nested deeper than {@link
   *     Config#getMaxResolveDepth()}
   */
  String fetch(Object sectionName, Object optionName);

  /**
   * Returns the last value of an option, with variables resolved, converted to a type.
   *
   * @param <T> the target type
   * @param sectionName the section name
   * @param optionName the option name
   * @param clazz the target type
   * @return the converted value; the zero value of a primitive type, or {@code null}, if the
   *     section or the option is missing
   * @throws IllegalArgumentException if the value cannot be converted
   * @throws CircularReferenceException if the variables are nested deeper than {@link
   *     Config#getMaxResolveDepth()}
   * @see #fetch(Object, Object)
   */
  <T> T fetch(Object sectionName, Object optionName, Class<T> clazz);

  /**
   * Returns the last value of an option, as stored.
   *
   * <p>The section is the last one with the given name. Variables are not resolved.
   *
   * @param sectionName the section name
   * @param optionName the option name
   * @return the value, or {@code null} if the section or the option is missing
   */
  String get(Object sectionName, Object optionName);

  /**
   * Returns the last value of an option, as stored, converted to a type.
   *
   * @param <T> the target type
   * @param sectionName the section name
   * @param optionName the option name
   * @param clazz the target type
   * @return the converted value; the zero value of a primitive type, or {@code null}, if the
   *     section or the option is missing
   * @throws IllegalArgumentException if the value cannot be converted
   * @see OptionMap#get(Object, Class)
   */
  <T> T get(Object sectionName, Object optionName, Class<T> clazz);

  /**
   * Sets an option value in a section.
   *
   * <p>The section is the last one with the given name; it is created if there is none. The value
   * replaces the last value of the option.
   *
   * @param sectionName the section name
   * @param optionName the option name
   * @param value the value, stored as in {@link OptionMap#put(String, Object)}
   * @return the replaced value, or {@code null} if there was none
   */
  String put(String sectionName, String optionName, Object value);

  /**
   * Removes the sections that have the name of the given section.
   *
   * <p>The removal is by name: every section with that name is removed, not only the given object.
   *
   * @param section the section whose name is removed
   * @return the first section that was removed, or {@code null} if there was none
   */
  Section remove(Profile.Section section);

  /**
   * Removes all the values of an option from a section.
   *
   * <p>The section is the last one with the given name.
   *
   * @param sectionName the section name
   * @param optionName the option name
   * @return the first value that was removed, or {@code null} if there was none or the section is
   *     missing
   */
  String removeOption(Object sectionName, Object optionName);

  /**
   * A section of a {@link Profile}: a named {@link OptionMap}.
   *
   * <p>A section knows the profile it belongs to. Its name can contain path separators, which makes
   * it a node in the section tree of the profile. The tree methods build the names of the related
   * sections from this name and look them up in the profile, so they only find sections that exist.
   * Variables in the values of a section are resolved by the profile, so they can refer to other
   * sections, see {@link Profile#fetch(Object, Object)}.
   *
   * @since 0.5.0
   */
  interface Section extends OptionMap {
    /**
     * Returns a child section.
     *
     * @param key the name of the child relative to this section, for example {@code b} for the
     *     child {@code a/b} of {@code a}
     * @return the child, or {@code null} if there is none; if several sections have the name, the
     *     last one
     */
    Section getChild(String key);

    /**
     * Returns the full name of the section.
     *
     * @return the name, including the path of its ancestors
     */
    String getName();

    /**
     * Returns the parent section.
     *
     * @return the section whose name is this name up to the last path separator, or {@code null} if
     *     the name has no path separator or there is no such section
     */
    Section getParent();

    /**
     * Returns the last part of the section name.
     *
     * @return the name after the last path separator, or the whole name if there is none
     */
    String getSimpleName();

    /**
     * Adds a new child section.
     *
     * <p>It works like {@link Profile#add(String)} with the name of this section, a path separator
     * and the key.
     *
     * @param key the name of the child relative to this section
     * @return the new section
     */
    Section addChild(String key);

    /**
     * Returns the names of the child sections.
     *
     * <p>Only direct children are listed, and each name is relative to this section. They come in
     * the order of the profile.
     *
     * @return a new array of names; it is empty if there are no children
     */
    String[] childrenNames();

    /**
     * Finds a descendant section by its path below this section.
     *
     * <p>The parts are joined with the path separator, so {@code lookup("b", "c")} on the section
     * {@code a} finds {@code a/b/c}.
     *
     * @param path the names of the sections on the way down from this section
     * @return the section, or {@code null} if there is none
     */
    Section lookup(String... path);

    /**
     * Removes a child section.
     *
     * <p>The sections with that name are removed. Their own children stay.
     *
     * @param key the name of the child relative to this section
     */
    void removeChild(String key);
  }
}
