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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Profile} that models a Windows registry as it appears in a {@code .reg} export file.
 *
 * <p>The sections are registry {@linkplain Key keys}. The name of a key is its full path, written
 * with {@link #KEY_SEPARATOR} between the parts and starting with a {@linkplain Hive hive}, for
 * example {@code HKEY_CURRENT_USER\Software\ini4j}. The options of a key are its values; the option
 * named {@link Key#DEFAULT_NAME} is the default value of the key. Each value also has a {@linkplain
 * Type type}.
 *
 * <p>The constants of this interface describe the {@code .reg} file format: a version header line
 * followed by the keys, in UTF-16 little endian text with {@code CR LF} line ends. The class that
 * reads, writes and, on Windows, exchanges data with the real registry is {@link Reg}.
 *
 * @see Reg
 * @see BasicRegistry
 * @since 0.5.0
 */
public interface Registry extends Profile {
  /**
   * The root keys of the registry, called hives.
   *
   * <p>Their names, as returned by {@link #toString()}, are the first part of a key path.
   *
   * @since 0.5.0
   */
  enum Hive {
    /** The root key {@code HKEY_CLASSES_ROOT}. */
    HKEY_CLASSES_ROOT,

    /** The root key {@code HKEY_CURRENT_CONFIG}. */
    HKEY_CURRENT_CONFIG,

    /** The root key {@code HKEY_CURRENT_USER}. */
    HKEY_CURRENT_USER,

    /** The root key {@code HKEY_LOCAL_MACHINE}. */
    HKEY_LOCAL_MACHINE,

    /** The root key {@code HKEY_USERS}. */
    HKEY_USERS;
  }

  // TODO handle delete operations with special Type
  /**
   * The type of a registry value.
   *
   * <p>The text form of a type, returned by {@link #toString()}, is the prefix that a {@code .reg}
   * file puts in front of the data of a value, for example {@code dword} in {@code dword:0000002a}.
   * A string value has no prefix, so the text of {@link #REG_SZ} is empty. This differs from {@link
   * #name()}.
   *
   * @since 0.5.0
   */
  enum Type {
    /** A value with no defined type. The data text is kept as written in the file. */
    REG_NONE("hex(0)"),

    /** A string, written in double quotes. This is the type of a value that has no prefix. */
    REG_SZ(""),

    /**
     * A string that can contain references to environment variables. The file holds the string as
     * hexadecimal UTF-16 little endian bytes, and the value in the map is the decoded text.
     */
    REG_EXPAND_SZ("hex(2)"),

    /** Binary data. The data text is kept as written in the file. */
    REG_BINARY("hex"),

    /**
     * A 32-bit number. The file holds it as eight hexadecimal digits, and the value in the map is
     * the decimal text.
     */
    REG_DWORD("dword"),

    /** A 32-bit number in big endian order. The data text is kept as written in the file. */
    REG_DWORD_BIG_ENDIAN("hex(5)"),

    /** A symbolic link. The data text is kept as written in the file. */
    REG_LINK("hex(6)"),

    /**
     * A list of strings. The file holds them as hexadecimal UTF-16 little endian bytes, and the map
     * has one value for each string.
     */
    REG_MULTI_SZ("hex(7)"),

    /** A resource list. The data text is kept as written in the file. */
    REG_RESOURCE_LIST("hex(8)"),

    /** A full resource descriptor. The data text is kept as written in the file. */
    REG_FULL_RESOURCE_DESCRIPTOR("hex(9)"),

    /** A resource requirements list. The data text is kept as written in the file. */
    REG_RESOURCE_REQUIREMENTS_LIST("hex(a)"),

    /** A 64-bit number. The data text is kept as written in the file. */
    REG_QWORD("hex(b)");
    private static final Map<String, Type> MAPPING;

    static {
      MAPPING = new HashMap<String, Type>();
      for (Type t : values()) {
        MAPPING.put(t.toString(), t);
      }
    }

    /** The character between the type prefix and the data of a value in a {@code .reg} file. */
    public static final char SEPARATOR_CHAR = ':';

    /** {@link #SEPARATOR_CHAR} as a string. */
    public static final String SEPARATOR = String.valueOf(SEPARATOR_CHAR);

    /**
     * The minus sign, which marks a removal in a {@code .reg} file. The parsers and formatters of
     * this library do not use it.
     */
    public static final char REMOVE_CHAR = '-';

    /** {@link #REMOVE_CHAR} as a string. */
    public static final String REMOVE = String.valueOf(REMOVE_CHAR);

    private final String _prefix;

    Type(String prefix) {
      _prefix = prefix;
    }

    /**
     * Returns the type for a text prefix.
     *
     * @param str the prefix as in {@link #toString()}, for example {@code "hex(7)"}; an empty text
     *     gives {@link #REG_SZ}
     * @return the type, or {@code null} if the prefix is unknown
     */
    public static Type fromString(String str) {
      return MAPPING.get(str);
    }

    /**
     * Returns the prefix that a {@code .reg} file uses for this type.
     *
     * @return the prefix; it is empty for {@link #REG_SZ}
     */
    @Override
    public String toString() {
      return _prefix;
    }
  }

  /** The escape character inside a quoted string of a {@code .reg} file. */
  char ESCAPE_CHAR = '\\';

  /** The character set of a {@code .reg} file: UTF-16 little endian. */
  Charset FILE_ENCODING = Charset.forName("UnicodeLittle");

  /** The character between the parts of a key path. */
  char KEY_SEPARATOR = '\\';

  /** The line separator of a {@code .reg} file: {@code CR LF}. */
  String LINE_SEPARATOR = "\r\n";

  /** The character between the type prefix and the data of a value. */
  char TYPE_SEPARATOR = ':';

  /** The text of the version header, the first line of a {@code .reg} file. */
  String VERSION = "Windows Registry Editor Version 5.00";

  /**
   * Returns the version header text.
   *
   * @return the header; it is {@link #VERSION} unless it was changed
   */
  String getVersion();

  /**
   * Sets the version header text.
   *
   * <p>{@link Reg} writes it as the first line when storing, and requires it to be the first line
   * when loading.
   *
   * @param value the header text
   */
  void setVersion(String value);

  /**
   * Returns the last key with the given path.
   *
   * @param key the key path
   * @return the key, or {@code null} if there is none
   */
  @Override
  Key get(Object key);

  /**
   * Returns the key with the given path at the given position.
   *
   * @param key the key path
   * @param index the position of the key among the keys with the same path
   * @return the key, or {@code null} if the path is not in the registry
   * @throws IndexOutOfBoundsException if the path is in the registry and the index is out of range
   */
  @Override
  Key get(Object key, int index);

  /**
   * Replaces the last key with the given path.
   *
   * @param key the key path
   * @param value the new key
   * @return the replaced key, or {@code null} if there was none
   */
  @Override
  Key put(String key, Section value);

  /**
   * Replaces the key with the given path at the given position.
   *
   * <p>The path must be in the registry already.
   *
   * @param key the key path
   * @param value the new key
   * @param index the position of the key to replace
   * @return the replaced key
   * @throws IndexOutOfBoundsException if the index is out of range
   * @throws NullPointerException if the path is not in the registry (this is what {@link
   *     BasicRegistry} does)
   */
  @Override
  Key put(String key, Section value, int index);

  /**
   * Removes all the keys with the given path.
   *
   * @param key the key path
   * @return the first key that was removed, or {@code null} if there was none
   */
  @Override
  Key remove(Object key);

  /**
   * Removes the key with the given path at the given position.
   *
   * @param key the key path
   * @param index the position of the key to remove
   * @return the removed key, or {@code null} if the path is not in the registry
   * @throws IndexOutOfBoundsException if the path is in the registry and the index is out of range
   */
  @Override
  Key remove(Object key, int index);

  /**
   * A registry key: a {@link Section} whose options are registry values.
   *
   * <p>A key also remembers the {@linkplain Type type} of each value. The type applies to all the
   * values of one name. The tree methods inherited from {@link Section} work on the key paths and
   * return keys.
   *
   * @since 0.5.0
   */
  interface Key extends Section {
    /** The name of the default value of a key. */
    String DEFAULT_NAME = "@";

    @Override
    Key getChild(String key);

    @Override
    Key getParent();

    /**
     * Returns the type of a value.
     *
     * @param key the value name
     * @return the type, or {@code null} if no type was set
     */
    Type getType(Object key);

    /**
     * Returns the type of a value, or a default.
     *
     * @param key the value name
     * @param defaulType the type to return if none was set
     * @return the type, or {@code defaulType} if none was set
     */
    Type getType(Object key, Type defaulType);

    @Override
    Key addChild(String key);

    @Override
    Key lookup(String... path);

    /**
     * Sets the type of a value.
     *
     * <p>When storing, a value with no type is written as {@link Type#REG_SZ}. Loading sets a type
     * only for the values that are not {@link Type#REG_SZ}.
     *
     * @param key the value name
     * @param type the type
     * @return the previous type, or {@code null} if none was set
     */
    Type putType(String key, Type type);

    /**
     * Removes the type of a value.
     *
     * @param key the value name
     * @return the removed type, or {@code null} if none was set
     */
    Type removeType(Object key);
  }
}
