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
 * Converts text to and from the backslash-escaped form of INI and options files.
 *
 * <p>This is the escaping that is used when {@link org.ini4j.Config#isEscape()} is on. The
 * characters backslash, tab, line feed, form feed, backspace, carriage return, colon and equals
 * sign are written as a backslash and a letter or sign ({@code \\}, {@code \t}, {@code \n}, {@code
 * \f}, {@code \b}, {@code \r}, {@code \:} and {@code \=}). Every other character outside the
 * printable ASCII range ({@code U+0020} to {@code U+007E}) is written as a backslash, the letter
 * {@code u} and four lower case hexadecimal digits. {@link #unescape(String)} reverses {@link
 * #escape(String)} for any text. {@link WinEscapeTool} and {@link RegEscapeTool} have their own
 * forms.
 *
 * <p>The instance is created once, when the class is first used, with the provider lookup described
 * in {@link IniParser#newInstance()}, using the name {@code org.ini4j.spi.EscapeTool}. A provider
 * that cannot be created makes the class initialization fail.
 *
 * <p>The class has no state, so it can be used from several threads.
 *
 * @since 0.4.1
 */
public class EscapeTool {
  private static final String ESCAPE_LETTERS = "\\tnfbr:=";
  private static final String ESCAPEABLE_CHARS = "\\\t\n\f\b\r:=";
  private static final char ESCAPE_CHAR = '\\';
  static final char[] HEX = "0123456789abcdef".toCharArray();
  private static final EscapeTool INSTANCE = ServiceFinder.findService(EscapeTool.class);
  private static final char ASCII_MIN = 0x20;
  private static final char ASCII_MAX = 0x7e;
  static final int HEX_DIGIT_MASK = 0x0f;
  static final int HEX_DIGIT_3_OFFSET = 4;
  static final int HEX_DIGIT_2_OFFSET = 8;
  static final int HEX_DIGIT_1_OFFSET = 12;
  static final int HEX_RADIX = 16;
  private static final int UNICODE_HEX_DIGITS = 4;
  static final char DOUBLE_QUOTE = '"';

  /**
   * Returns the shared instance.
   *
   * @return the instance
   */
  public static EscapeTool getInstance() {
    return INSTANCE;
  }

  /**
   * Escapes a text.
   *
   * @param line the text to escape
   * @return the escaped text
   */
  public String escape(String line) {
    int len = line.length();
    StringBuilder buffer = new StringBuilder(len * 2);

    for (int i = 0; i < len; i++) {
      char c = line.charAt(i);
      int idx = ESCAPEABLE_CHARS.indexOf(c);

      if (idx >= 0) {
        buffer.append(ESCAPE_CHAR);
        buffer.append(ESCAPE_LETTERS.charAt(idx));
      } else {
        if ((c < ASCII_MIN) || (c > ASCII_MAX)) {
          escapeBinary(buffer, c);
        } else {
          buffer.append(c);
        }
      }
    }

    return buffer.toString();
  }

  /**
   * Puts a text in double quotes.
   *
   * <p>A backslash and a double quote inside the text get a backslash in front of them.
   *
   * @param value the text to quote
   * @return the quoted text; {@code null} and an empty text are returned as they are, without
   *     quotes
   */
  public String quote(String value) {
    String ret = value;

    if ((value != null) && (value.length() != 0)) {
      StringBuilder buff = new StringBuilder();

      buff.append(DOUBLE_QUOTE);
      for (int i = 0; i < value.length(); i++) {
        char c = value.charAt(i);

        if ((c == ESCAPE_CHAR) || (c == DOUBLE_QUOTE)) {
          buff.append(ESCAPE_CHAR);
        }

        buff.append(c);
      }

      buff.append(DOUBLE_QUOTE);
      ret = buff.toString();
    }

    return ret;
  }

  /**
   * Unescapes a text.
   *
   * <p>A backslash followed by a letter or sign of the table in the description of this class gives
   * the character. A backslash followed by {@code u} and four hexadecimal digits gives the
   * character with that code. A backslash before any other character is dropped and the character
   * stays.
   *
   * @param line the escaped text
   * @return the unescaped text
   * @throws IllegalArgumentException if an escape with {@code u} is malformed
   * @throws StringIndexOutOfBoundsException if the text ends with a single backslash
   */
  public String unescape(String line) {
    int n = line.length();
    StringBuilder buffer = new StringBuilder(n);
    int i = 0;

    while (i < n) {
      char c = line.charAt(i++);

      if (c == ESCAPE_CHAR) {
        c = line.charAt(i++);
        int next = unescapeBinary(buffer, c, line, i);

        if (next == i) {
          int idx = ESCAPE_LETTERS.indexOf(c);

          if (idx >= 0) {
            c = ESCAPEABLE_CHARS.charAt(idx);
          }

          buffer.append(c);
        } else {
          i = next;
        }
      } else {
        buffer.append(c);
      }
    }

    return buffer.toString();
  }

  /**
   * Removes the quotes from a quoted text.
   *
   * <p>The first and the last character are removed, whatever they are. The text is expected to
   * have the form that {@link #quote(String)} writes, where a backslash before a backslash or a
   * double quote is removed. Other uses of the backslash are not handled consistently: a backslash
   * before any other character is dropped, and the next backslash after it is then kept.
   *
   * @param value the quoted text
   * @return the text without the quotes
   */
  public String unquote(String value) {
    StringBuilder buff = new StringBuilder();
    boolean escape = false;

    for (int i = 1; i < (value.length() - 1); i++) {
      char c = value.charAt(i);

      if (c == ESCAPE_CHAR) {
        if (!escape) {
          escape = true;

          continue;
        }

        escape = false;
      }

      buff.append(c);
    }

    return buff.toString();
  }

  /**
   * Writes a character that has no short form as a {@code u} escape.
   *
   * <p>A subclass overrides it to use another form.
   *
   * @param buff the buffer to write to
   * @param c the character
   */
  void escapeBinary(StringBuilder buff, char c) {
    buff.append("\\u");
    buff.append(HEX[(c >>> HEX_DIGIT_1_OFFSET) & HEX_DIGIT_MASK]);
    buff.append(HEX[(c >>> HEX_DIGIT_2_OFFSET) & HEX_DIGIT_MASK]);
    buff.append(HEX[(c >>> HEX_DIGIT_3_OFFSET) & HEX_DIGIT_MASK]);
    buff.append(HEX[c & HEX_DIGIT_MASK]);
  }

  /**
   * Reads an escape that has more than one character after the backslash.
   *
   * <p>A subclass overrides it to read other forms. When the type is not one it knows, it returns
   * {@code index} and the caller treats the type as a plain escaped character.
   *
   * @param buff the buffer to write the character to
   * @param escapeType the character after the backslash
   * @param line the whole text
   * @param index the position after {@code escapeType}
   * @return the position after the escape, or {@code index} if this is not an escape of this kind
   * @throws IllegalArgumentException if the escape is malformed
   */
  int unescapeBinary(StringBuilder buff, char escapeType, String line, int index) {
    int ret = index;

    if (escapeType == 'u') {
      try {
        buff.append(
            (char) Integer.parseInt(line.substring(index, index + UNICODE_HEX_DIGITS), HEX_RADIX));
        ret = index + UNICODE_HEX_DIGITS;
      } catch (Exception x) {
        throw new IllegalArgumentException("Malformed \\uxxxx encoding.", x);
      }
    }

    return ret;
  }
}
