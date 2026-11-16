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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import org.ini4j.spi.WinEscapeTool;

/**
 * An {@link Ini} set up for the files that Windows applications use.
 *
 * <p>A new {@code Wini} has its own copy of the global {@link Config} with these differences:
 *
 * <ul>
 *   <li>backslash escapes are off (see {@link Config#isEscape()}), and so is joining of lines that
 *       end with a backslash,
 *   <li>options before the first section are accepted and kept in a section named {@link
 *       Config#getGlobalSectionName()},
 *   <li>an option without a value is allowed (see {@link Config#isEmptyOption()}),
 *   <li>an option name keeps only its last value (see {@link Config#isMultiOption()}),
 *   <li>the path separator of section names is a backslash, {@link #PATH_SEPARATOR}.
 * </ul>
 *
 * <p>Since escaping is off, a backslash in a name or a value is read and written as it is. The
 * methods {@link #escape(String)} and {@link #unescape(String)} are there for the cases where you
 * want to handle the Windows forms of escapes yourself.
 *
 * @see Ini
 * @since 0.5.0
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
public class Wini extends Ini {
  private static final long serialVersionUID = -2781377824232440728L;

  /**
   * The path separator of section names in a {@code Wini}: a backslash. It hides {@link
   * Profile#PATH_SEPARATOR}.
   */
  public static final char PATH_SEPARATOR = '\\';

  /** Creates an empty {@code Wini}. */
  public Wini() {
    Config cfg = Config.getGlobal().clone();

    cfg.setEscape(false);
    cfg.setEscapeNewline(false);
    cfg.setGlobalSection(true);
    cfg.setEmptyOption(true);
    cfg.setMultiOption(false);
    cfg.setPathSeparator(PATH_SEPARATOR);
    setConfig(cfg);
  }

  /**
   * Creates a {@code Wini}, remembers the file and loads it.
   *
   * @param input the file to read and later to write
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Wini(File input) throws IOException, InvalidFileFormatException {
    this();
    setFile(input);
    load();
  }

  /**
   * Creates a {@code Wini} and loads it from a URL.
   *
   * @param input the location to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Wini(URL input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Creates a {@code Wini} and loads it from a stream.
   *
   * @param input the stream to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Wini(InputStream input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Creates a {@code Wini} and loads it from a reader.
   *
   * @param input the reader to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Wini(Reader input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Escapes a text in the Windows style.
   *
   * <p>A backslash, tab, line feed, form feed, backspace, carriage return, colon and equals sign
   * are written as a backslash and a letter ({@code \\}, {@code \t}, {@code \n}, {@code \f}, {@code
   * \b}, {@code \r}, {@code \:} and {@code \=}). Any other character outside the printable ASCII
   * range is written as {@code \x} and two hexadecimal digits, which hold only the low byte of the
   * character.
   *
   * @param value the text to escape
   * @return the escaped text
   */
  public String escape(String value) {
    return WinEscapeTool.getInstance().escape(value);
  }

  /**
   * Reverses {@link #escape(String)}.
   *
   * <p>Besides the forms that {@link #escape(String)} writes, it reads {@code \o} followed by three
   * octal digits and {@code \x} followed by two hexadecimal digits as a character with that code. A
   * backslash before any other character is dropped.
   *
   * @param value the text to unescape
   * @return the unescaped text
   * @throws IllegalArgumentException if a {@code \x} or {@code \o} sequence is malformed
   */
  public String unescape(String value) {
    return WinEscapeTool.getInstance().unescape(value);
  }
}
