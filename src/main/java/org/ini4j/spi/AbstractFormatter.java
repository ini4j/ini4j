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

import java.io.PrintWriter;
import org.ini4j.Config;

/**
 * The common part of the formatters: it writes comments and options as text.
 *
 * <p>The output goes to a {@link java.io.PrintWriter}, which hides write errors instead of throwing
 * them. The text is written as the {@link org.ini4j.Config} says.
 *
 * @since 0.5.0
 */
abstract class AbstractFormatter implements HandlerBase {
  private static final char OPERATOR = '=';
  private static final char COMMENT = '#';
  private static final char SPACE = ' ';
  private Config _config = Config.getGlobal();
  private boolean _header = true;
  private PrintWriter _output;

  /**
   * Writes a comment.
   *
   * <p>The comment is written as lines that start with {@code #}, one for each line of the comment,
   * where a line ends at the line separator of the configuration ({@link
   * org.ini4j.Config#getLineSeparator()}). Nothing is written if {@link
   * org.ini4j.Config#isComment()} is off, or if the comment is {@code null} or empty.
   *
   * <p>The first call of this method, if it comes before any option or section, is for the header
   * comment. A header comment is followed by an empty line, and it is written only if {@link
   * org.ini4j.Config#isHeaderComment()} is on.
   *
   * @param comment the comment, or {@code null} for none
   */
  @Override
  public void handleComment(String comment) {
    if (getConfig().isComment()
        && (!_header || getConfig().isHeaderComment())
        && (comment != null)
        && (comment.length() != 0)) {
      for (String line : comment.split(getConfig().getLineSeparator())) {
        getOutput().print(COMMENT);
        getOutput().print(line);
        getOutput().print(getConfig().getLineSeparator());
      }

      if (_header) {
        getOutput().print(getConfig().getLineSeparator());
      }
    }

    _header = false;
  }

  /**
   * Writes an option.
   *
   * <p>The option is written as {@code name=value} if {@link org.ini4j.Config#isStrictOperator()}
   * is on, and as {@code name = value} otherwise. The name, and the value unless {@link
   * org.ini4j.Config#isEscapeKeyOnly()} is on, are escaped if {@link org.ini4j.Config#isEscape()}
   * is on. An option whose value is {@code null} is written with an empty value if {@link
   * org.ini4j.Config#isEmptyOption()} is on, and it is left out otherwise. Every line ends with the
   * line separator of the configuration.
   *
   * @param optionName the name of the option
   * @param optionValue the value, or {@code null} for an option without a value
   */
  @Override
  public void handleOption(String optionName, String optionValue) {
    if (getConfig().isStrictOperator()) {
      if (getConfig().isEmptyOption() || (optionValue != null)) {
        getOutput().print(escapeKey(optionName));
        getOutput().print(OPERATOR);
      }

      if (optionValue != null) {
        getOutput().print(escapeValue(optionValue));
      }

      if (getConfig().isEmptyOption() || (optionValue != null)) {
        getOutput().print(getConfig().getLineSeparator());
      }
    } else {
      String value = ((optionValue == null) && getConfig().isEmptyOption()) ? "" : optionValue;

      if (value != null) {
        getOutput().print(escapeKey(optionName));
        getOutput().print(SPACE);
        getOutput().print(OPERATOR);
        getOutput().print(SPACE);
        getOutput().print(escapeValue(value));
        getOutput().print(getConfig().getLineSeparator());
      }
    }

    setHeader(false);
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
   * Returns the output.
   *
   * @return the writer that receives the text
   */
  protected PrintWriter getOutput() {
    return _output;
  }

  /**
   * Sets the output.
   *
   * @param value the writer that receives the text; it is flushed at the end but never closed
   */
  protected void setOutput(PrintWriter value) {
    _output = value;
  }

  void setHeader(boolean value) {
    _header = value;
  }

  String escapeKey(String input) {
    return getConfig().isEscape() ? EscapeTool.getInstance().escape(input) : input;
  }

  String escapeValue(String input) {
    return getConfig().isEscape() && !getConfig().isEscapeKeyOnly()
        ? EscapeTool.getInstance().escape(input)
        : input;
  }
}
