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
 * Receives the events of a file of options without sections, in the order of the file.
 *
 * <p>The same interface is used in both directions. An {@link OptionsParser} calls it while it
 * reads a file, and an {@link org.ini4j.Options} calls it while it is stored. The calls are {@link
 * #startOptions()}, then {@link #handleOption(String, String)} for each option, and {@link
 * #endOptions()}.
 *
 * <p>{@link #handleComment(String)} can come at any point between the start and the end. A comment
 * is reported just before the option it belongs to, and a comment at the end of the file arrives
 * before {@link #endOptions()}. The order is promised only for a successful parse. If the parser
 * throws an exception, {@link #endOptions()} may be missing.
 *
 * <p>When a file is stored, the calls between {@link #startOptions()} and {@link #endOptions()} are
 * the header comment, then for each option its comment and one {@link #handleOption(String,
 * String)} call for each value. In this direction a comment can be {@code null}, which means there
 * is none. When {@link org.ini4j.Config#isMultiOption()} is off, only one value of each option is
 * sent, the first one.
 *
 * @see OptionsParser
 * @see OptionsFormatter
 * @see OptionsBuilder
 * @since 0.5.0
 */
public interface OptionsHandler extends HandlerBase {
  /** Ends the file. The parser calls it after the last option. */
  void endOptions();

  /**
   * Receives a comment.
   *
   * <p>The text is as described for {@link IniHandler#handleComment(String)}, with the comment
   * characters of this format.
   *
   * @param comment the text of the comment
   */
  @Override
  void handleComment(String comment);

  /**
   * Receives an option.
   *
   * @param optionName the name of the option, never {@code null}
   * @param optionValue the value of the option; it is {@code null} for an option without a value,
   *     which the parser allows only when {@link org.ini4j.Config#isEmptyOption()} is on
   */
  @Override
  void handleOption(String optionName, String optionValue);

  /** Starts the file. It is the first call. */
  void startOptions();
}
