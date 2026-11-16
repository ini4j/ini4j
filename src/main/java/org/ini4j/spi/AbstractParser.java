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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Locale;
import org.ini4j.Config;
import org.ini4j.InvalidFileFormatException;

/**
 * The common part of the parsers: it holds the configuration and parses the lines with options.
 *
 * <p>A subclass tells the characters that begin a comment line and the characters that separate an
 * option name from its value. The parsing of an option line follows the {@link org.ini4j.Config}
 * flags such as {@link org.ini4j.Config#isEscape()}, {@link org.ini4j.Config#isEmptyOption()} and
 * {@link org.ini4j.Config#isLowerCaseOption()}.
 *
 * @since 0.5.0
 */
abstract class AbstractParser {
  private final String _comments;
  private Config _config = Config.getGlobal();
  private final String _operators;

  /**
   * Creates a parser.
   *
   * @param operators the characters that separate the name of an option from its value; the first
   *     one in the line that is not escaped with a backslash counts
   * @param comments the characters that begin a comment line
   */
  protected AbstractParser(String operators, String comments) {
    _operators = operators;
    _comments = comments;
  }

  /**
   * Returns the configuration.
   *
   * @return the configuration in use; it is {@link org.ini4j.Config#getGlobal()} until another one
   *     is set
   */
  protected Config getConfig() {
    return _config;
  }

  /**
   * Sets the configuration.
   *
   * @param value the configuration to use from now on
   */
  protected void setConfig(Config value) {
    _config = value;
  }

  /**
   * Reports a line that cannot be parsed.
   *
   * @param line the text of the line
   * @param lineNumber the number of the line in the input
   * @throws InvalidFileFormatException always; the message contains the line number and the line
   */
  protected void parseError(String line, int lineNumber) throws InvalidFileFormatException {
    throw new InvalidFileFormatException("parse error (at line: " + lineNumber + "): " + line);
  }

  IniSource newIniSource(InputStream input, HandlerBase handler) {
    return new IniSource(input, handler, _comments, getConfig());
  }

  IniSource newIniSource(Reader input, HandlerBase handler) {
    return new IniSource(input, handler, _comments, getConfig());
  }

  IniSource newIniSource(URL input, HandlerBase handler) throws IOException {
    return new IniSource(input, handler, _comments, getConfig());
  }

  /**
   * Parses a line with an option and sends it to the handler.
   *
   * <p>The name is the text before the first operator that is not preceded by a backslash. When
   * there is an operator, the name and the value are unescaped according to the configuration and
   * trimmed. A line without an operator is an option without a value if {@link
   * org.ini4j.Config#isEmptyOption()} is on, with the whole line as its name, and an error
   * otherwise. An empty name is an error. With {@link org.ini4j.Config#isLowerCaseOption()} the
   * name is converted to lower case.
   *
   * @param line the trimmed line
   * @param handler the receiver of the option
   * @param lineNumber the number of the line, for error messages
   * @throws InvalidFileFormatException if the line is not a valid option
   */
  void parseOptionLine(String line, HandlerBase handler, int lineNumber)
      throws InvalidFileFormatException {
    int idx = indexOfOperator(line);
    String name = null;
    String value = null;

    if (idx < 0) {
      if (getConfig().isEmptyOption()) {
        name = line;
      } else {
        parseError(line, lineNumber);
      }
    } else {
      name = unescapeKey(line.substring(0, idx)).trim();
      value = unescapeValue(line.substring(idx + 1)).trim();
    }

    if (name == null || name.length() == 0) {
      parseError(line, lineNumber);
    }

    if (getConfig().isLowerCaseOption()) {
      name = (name != null) ? name.toLowerCase(Locale.getDefault()) : null;
    }

    handler.handleOption(name, value);
  }

  String unescapeKey(String line) {
    return getConfig().isEscape() ? EscapeTool.getInstance().unescape(line) : line;
  }

  String unescapeValue(String line) {
    return (getConfig().isEscape() && !getConfig().isEscapeKeyOnly())
        ? EscapeTool.getInstance().unescape(line)
        : line;
  }

  private int indexOfOperator(String line) {
    int idx = -1;

    for (char c : _operators.toCharArray()) {
      int index = line.indexOf(c);

      while (index >= 0) {
        if (((index == 0) || (line.charAt(index - 1) != '\\')) && ((idx == -1) || (index < idx))) {
          idx = index;

          break;
        }

        index = (index == (line.length() - 1)) ? -1 : line.indexOf(c, index + 1);
      }

      // if ((index >= 0) && ((index == 0) || (line.charAt(index - 1) != '\\')) && ((idx == -1) ||
      // (index < idx)))
      // {
      //     idx = index;
      // }
    }

    return idx;
  }
}
