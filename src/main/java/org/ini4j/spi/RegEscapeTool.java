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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.ini4j.Registry;
import org.ini4j.Registry.Type;

/**
 * Converts registry values between the text of a {@code .reg} file and their type and values.
 *
 * <p>The text of a value in a {@code .reg} file is either a quoted string, or a type prefix, a
 * colon and the data (see {@link org.ini4j.Registry.Type}). {@link #decode(String)} and {@link
 * #encode(TypeValuesPair)} convert between that text and a {@link TypeValuesPair}. Quoting is
 * inherited from {@link EscapeTool}.
 *
 * <p>The instance is created once, when the class is first used, with the provider lookup described
 * in {@link IniParser#newInstance()}, using the name {@code org.ini4j.spi.RegEscapeTool}. A
 * provider that cannot be created makes the class initialization fail.
 *
 * <p>The class has no state, so it can be used from several threads.
 *
 * @see org.ini4j.Reg
 * @since 0.5.0
 */
public class RegEscapeTool extends EscapeTool {
  private static final RegEscapeTool INSTANCE = ServiceFinder.findService(RegEscapeTool.class);
  private static final Charset HEX_CHARSET = StandardCharsets.UTF_16LE;
  private static final int LOWER_DIGIT = 0x0f;
  private static final int UPPER_DIGIT = 0xf0;
  private static final int DIGIT_SIZE = 4;

  /**
   * Returns the shared instance.
   *
   * @return the instance
   */
  public static final RegEscapeTool getInstance() {
    return INSTANCE;
  }

  /**
   * Splits the text of a value into its type and values.
   *
   * <p>A text that starts with a double quote is a {@link org.ini4j.Registry.Type#REG_SZ} string,
   * and a text without a colon is one too. For a string, the first and last character are removed,
   * whatever they are, and the escapes are resolved. The types {@link
   * org.ini4j.Registry.Type#REG_EXPAND_SZ} and {@link org.ini4j.Registry.Type#REG_MULTI_SZ} hold
   * hexadecimal bytes of UTF-16 little endian text, which are decoded; the multi-string gives one
   * value for each string. A {@link org.ini4j.Registry.Type#REG_DWORD} gives its decimal text. For
   * any other type, the data after the colon is the only value, as written.
   *
   * @param raw the text of the value
   * @return the type and the values
   * @throws NullPointerException if the type prefix is not known
   * @throws NumberFormatException if a {@code dword} is not a hexadecimal number
   * @throws StringIndexOutOfBoundsException if the text is empty, or the data of a {@code
   *     REG_EXPAND_SZ} value is empty
   */
  public TypeValuesPair decode(String raw) {
    Type type = type(raw);
    String value =
        (type == Type.REG_SZ) ? unquote(raw) : raw.substring(type.toString().length() + 1);
    String[] values;

    switch (type) {
      case REG_EXPAND_SZ:
      case REG_MULTI_SZ:
        byte[] bytes = binary(value);
        value = new String(bytes, 0, bytes.length - 2, HEX_CHARSET);
        break;

      case REG_DWORD:
        value = String.valueOf(Long.parseLong(value, HEX_RADIX));
        break;

      case REG_SZ:
        break;

      default:
        break;
    }

    if (type == Type.REG_MULTI_SZ) {
      values = splitMulti(value);
    } else {
      values = new String[] {value};
    }

    return new TypeValuesPair(type, values);
  }

  /**
   * Writes a type and values as the text of a value.
   *
   * <p>A {@link org.ini4j.Registry.Type#REG_SZ} string is quoted, and only its first value is used.
   * An empty string is written as empty text, without quotes, as {@link #quote(String)} does, and
   * {@link #decode(String)} cannot read that either. For the other types the text is the prefix of
   * the type, a colon and the data, written in the form that {@link #decode(String)} reads, except
   * that an empty {@code REG_EXPAND_SZ} value is written as empty data, which {@link
   * #decode(String)} cannot read. Only the first value is used except for {@link
   * org.ini4j.Registry.Type#REG_MULTI_SZ}, which writes all of them.
   *
   * @param data the type and values
   * @return the text, or {@code null} if the first value is {@code null}
   * @throws NumberFormatException if the type is {@code REG_DWORD} and the value is not a number
   * @throws ArrayIndexOutOfBoundsException if the array of values is empty
   */
  public String encode(TypeValuesPair data) {
    String ret = null;

    if (data.getType() == Type.REG_SZ) {
      ret = quote(data.getValues()[0]);
    } else if (data.getValues()[0] != null) {
      ret = encode(data.getType(), data.getValues());
    }

    return ret;
  }

  byte[] binary(String value) {
    byte[] bytes = new byte[value.length()];
    int idx = 0;
    int shift = DIGIT_SIZE;

    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);

      if (Character.isWhitespace(c)) {
        continue;
      }

      if (c == ',') {
        idx++;
        shift = DIGIT_SIZE;
      } else {
        int digit = Character.digit(c, HEX_RADIX);

        if (digit >= 0) {
          bytes[idx] |= digit << shift;
          shift = 0;
        }
      }
    }

    return Arrays.copyOfRange(bytes, 0, idx + 1);
  }

  String encode(Type type, String[] values) {
    StringBuilder buff = new StringBuilder();

    buff.append(type.toString());
    buff.append(Type.SEPARATOR_CHAR);
    switch (type) {
      case REG_EXPAND_SZ:
        buff.append(hexadecimal(values[0]));
        break;

      case REG_DWORD:
        buff.append(String.format("%08x", Long.parseLong(values[0])));
        break;

      case REG_MULTI_SZ:
        int n = values.length;

        for (int i = 0; i < n; i++) {
          buff.append(hexadecimal(values[i]));
          buff.append(',');
        }

        buff.append("00,00");
        break;

      default:
        buff.append(values[0]);
        break;
    }

    return buff.toString();
  }

  String hexadecimal(String value) {
    StringBuilder buff = new StringBuilder();

    if ((value != null) && (value.length() != 0)) {
      byte[] bytes = value.getBytes(HEX_CHARSET);

      for (byte b : bytes) {
        buff.append(Character.forDigit((b & UPPER_DIGIT) >> DIGIT_SIZE, HEX_RADIX));
        buff.append(Character.forDigit(b & LOWER_DIGIT, HEX_RADIX));
        buff.append(',');
      }

      buff.append("00,00");
    }

    return buff.toString();
  }

  Registry.Type type(String raw) {
    Registry.Type type;

    if (raw.charAt(0) == DOUBLE_QUOTE) {
      type = Registry.Type.REG_SZ;
    } else {
      int idx = raw.indexOf(Registry.TYPE_SEPARATOR);

      type = (idx < 0) ? Registry.Type.REG_SZ : Registry.Type.fromString(raw.substring(0, idx));
    }

    return type;
  }

  private String[] splitMulti(String value) {
    int len = value.length();
    int start;
    int end;
    int n = 0;

    start = 0;
    for (end = value.indexOf(0, start); end >= 0; end = value.indexOf(0, start)) {
      n++;
      start = end + 1;
      if (start >= len) {
        break;
      }
    }

    String[] values = new String[n];

    start = 0;
    for (int i = 0; i < n; i++) {
      end = value.indexOf(0, start);
      values[i] = value.substring(start, end);
      start = end + 1;
    }

    return values;
  }
}
