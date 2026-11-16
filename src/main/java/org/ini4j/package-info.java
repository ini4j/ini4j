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
/**
 * The main API of ini4j: reading, changing and writing INI files, option files and Windows registry
 * files.
 *
 * <p>The file formats and their classes:
 *
 * <ul>
 *   <li>{@link org.ini4j.Ini} is an INI file with sections. {@link org.ini4j.Wini} is the same with
 *       the settings of Windows files.
 *   <li>{@link org.ini4j.Options} is a file of options without sections.
 *   <li>{@link org.ini4j.Reg} is a Windows registry export file ({@code .reg}), which is modeled by
 *       {@link org.ini4j.Registry}.
 * </ul>
 *
 * <p>{@link org.ini4j.ConfigParser} offers the API of the Python {@code ConfigParser} on top of
 * {@code Ini}, and {@link org.ini4j.IniPreferences} and {@link org.ini4j.IniPreferencesFactory}
 * serve the {@code java.util.prefs} API from an INI file. The behavior of parsing and writing is
 * set with {@link org.ini4j.Config}. The parsers, the formatters and the events between them are in
 * the package {@code org.ini4j.spi}.
 *
 * <p>This example loads a file, reads a value, changes it and stores the file:
 *
 * <pre>{@code
 * Ini ini = new Ini(new File("dwarfs.ini"));
 * int age = ini.get("happy", "age", int.class);
 *
 * ini.put("happy", "age", age + 1);
 * ini.store();
 * }</pre>
 *
 * <p>These words have a fixed meaning in the documentation:
 *
 * <dl>
 *   <dt>section
 *   <dd>A named group of options, written as {@code [name]} in a file. The type is {@link
 *       org.ini4j.Profile.Section}. A section name can contain the path separator, such as {@code
 *       a/b}, which makes the section a child of the section {@code a}.
 *   <dt>option
 *   <dd>A named value in a section, written as {@code name=value}. The names and the values are
 *       text. A group of options is an {@link org.ini4j.OptionMap}.
 *   <dt>multi-valued option
 *   <dd>An option name that has more than one value, for example because the name is repeated in
 *       the file. The values are indexed from zero, see {@link org.ini4j.MultiMap} and {@link
 *       org.ini4j.Config#isMultiOption()}.
 *   <dt>profile
 *   <dd>The whole content of an INI file: its sections and the comment of the file. The type is
 *       {@link org.ini4j.Profile}.
 *   <dt>global section
 *   <dd>The section that holds the options that come before the first section header, if {@link
 *       org.ini4j.Config#isGlobalSection()} is on. It is named by {@link
 *       org.ini4j.Config#getGlobalSectionName()}.
 *   <dt>registry key
 *   <dd>A section of a registry file. Its name is the full path of the key, and its options are the
 *       values of the key, each with a type. The type is {@link org.ini4j.Registry.Key}.
 * </dl>
 */
package org.ini4j;
