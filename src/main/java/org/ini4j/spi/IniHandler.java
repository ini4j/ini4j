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
 * Receives the events of an INI file, in the order of the file.
 *
 * <p>The same interface is used in both directions. A {@link IniParser} calls it while it reads a
 * file, and an {@link org.ini4j.Ini} calls it while it is stored, so a handler is either a builder
 * of the content ({@link IniBuilder}) or a writer of it ({@link IniFormatter}).
 *
 * <p>The calls come in this order: {@link #startIni()}, then the sections, then {@link #endIni()}.
 * A section is {@link #startSection(String)}, any number of {@link #handleOption(String, String)}
 * calls for its options in file order, and {@link #endSection()}. Options never come outside a
 * section. If {@link org.ini4j.Config#isGlobalSection()} is on, the parser starts a section for the
 * options that come before the first header, and uses {@link
 * org.ini4j.Config#getGlobalSectionName()} as its name.
 *
 * <p>{@link #handleComment(String)} can come at any point between those calls. A comment is
 * reported just before the section or the option it belongs to, so the comment above a section
 * header arrives before the {@link #endSection()} of the section before it. A comment at the end of
 * the file arrives before the last {@link #endSection()} (or before {@link #endIni()} if there is
 * no section). The order is promised only for a successful parse. If the parser throws an
 * exception, the closing calls may be missing.
 *
 * <p>When a file is stored, the calls between {@link #startIni()} and {@link #endIni()} are the
 * header comment, then for each section its comment, {@link #startSection(String)}, for each option
 * its comment and one {@link #handleOption(String, String)} call for each value, and {@link
 * #endSection()}. In this direction a comment can be {@code null}, which means there is none. The
 * {@link org.ini4j.Config} flags can leave things out: a section without options is skipped when
 * {@link org.ini4j.Config#isEmptySection()} is off, and when {@link
 * org.ini4j.Config#isMultiOption()} is off only one value of each option is sent, the last one.
 *
 * @see IniParser
 * @see IniFormatter
 * @see IniBuilder
 * @since 0.5.0
 */
public interface IniHandler extends HandlerBase {
  /** Ends the file. The parser calls it after the last section. */
  void endIni();

  /** Ends the current section. */
  void endSection();

  /**
   * Receives a comment.
   *
   * <p>When a file is parsed, it is the text of the comment lines without the leading comment
   * character. Each line is trimmed before the comment character is removed, so spaces after the
   * comment character stay in the text. The lines are joined with the line separator of the
   * configuration, and the last separator is cut by one character only, so with a separator of two
   * characters, such as {@code CR LF}, the text ends with one of them. A blank line ends a comment,
   * so one run of comment lines is one comment. A comment line with nothing after the comment
   * character gives an empty line in the text. When a file is stored, it is the comment of the
   * header, a section or an option, and it is {@code null} if there is none.
   *
   * @param comment the text of the comment
   */
  @Override
  void handleComment(String comment);

  /**
   * Receives an option of the current section.
   *
   * @param optionName the name of the option, never {@code null}
   * @param optionValue the value of the option; it is {@code null} for an option without a value,
   *     which the parser allows only when {@link org.ini4j.Config#isEmptyOption()} is on
   */
  @Override
  void handleOption(String optionName, String optionValue);

  /** Starts the file. It is the first call. */
  void startIni();

  /**
   * Starts a section.
   *
   * @param sectionName the name of the section
   */
  void startSection(String sectionName);
}
