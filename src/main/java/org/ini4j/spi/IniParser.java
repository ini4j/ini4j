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
 * Parses INI files and sends the events to an {@link IniHandler}.
 *
 * <p>A line that starts with {@code [} is a section header, and it must end with {@code ]}. The
 * name between the brackets is trimmed and unescaped. Any other line is an option, written as
 * {@code name=value} or {@code name:value}. A line that starts with {@code ;} or {@code #} is a
 * comment. Comments exist only on lines of their own, so a comment after an option or after a
 * header is part of the value, or an error. Blank lines are skipped, and lines are trimmed. The
 * {@link org.ini4j.Config} flags decide a number of details, for example {@link
 * org.ini4j.Config#isGlobalSection()}, {@link org.ini4j.Config#isUnnamedSection()}, {@link
 * org.ini4j.Config#isLowerCaseSection()}, {@link org.ini4j.Config#isEscapeNewline()} and {@link
 * org.ini4j.Config#isInclude()}.
 *
 * <p>The events and their order are described in {@link IniHandler}. A line that is not valid
 * throws an {@link org.ini4j.InvalidFileFormatException} whose message has the line number and the
 * line.
 *
 * <p>The input is closed when it has been read to the end. If an error is found before that, it is
 * left open.
 *
 * @see IniHandler
 * @see IniFormatter
 * @since 0.5.0
 */
public class IniParser extends AbstractParser {
  private static final String COMMENTS = ";#";
  private static final String OPERATORS = ":=";
  static final char SECTION_BEGIN = '[';
  static final char SECTION_END = ']';

  /**
   * Creates a parser that uses {@link org.ini4j.Config#getGlobal()}.
   *
   * <p>Use {@link #newInstance()} or {@link #newInstance(org.ini4j.Config)} unless you want to
   * bypass the provider lookup.
   */
  public IniParser() {
    super(OPERATORS, COMMENTS);
  }

  /**
   * Creates a parser of this class, or of the class that a provider names.
   *
   * <p>The class is looked up in this order, and the first source that has a value wins:
   *
   * <ul>
   *   <li>the system property whose name is the fully qualified name of the class, for example
   *       {@code org.ini4j.spi.IniParser},
   *   <li>the first line of the resource {@code META-INF/services/} followed by that name, found
   *       with the context class loader of the current thread. The line is trimmed and cut at the
   *       first white space or {@code #}. A blank first line means that there is no provider, and
   *       later lines are never read. A line that starts with {@code #} gives an empty name,
   *   <li>the class itself.
   * </ul>
   *
   * <p>If the system property cannot be read because of a security manager, it is skipped. A
   * resource that cannot be read counts as not found. If the thread has no context class loader,
   * the system class loader is used. There is no fallback after a bad name: if the provider class
   * is not found, or it is not usable, the call fails. The provider must be a subclass that has a
   * public constructor with no arguments. The call throws an {@link IllegalArgumentException} if
   * the class is not found, or if it cannot be created for any reason, including an exception that
   * its constructor throws. Only an {@link Error} that the Java runtime throws while it loads or
   * links the class, such as a {@link NoClassDefFoundError} or an {@link
   * ExceptionInInitializerError}, is not wrapped.
   *
   * @return a new parser
   * @throws IllegalArgumentException if the provider cannot be found or created
   */
  public static IniParser newInstance() {
    return ServiceFinder.findService(IniParser.class);
  }

  /**
   * Creates a parser as {@link #newInstance()} does, and gives it a configuration.
   *
   * @param config the configuration to use
   * @return a new parser
   * @throws IllegalArgumentException if the provider cannot be found or created
   */
  public static IniParser newInstance(Config config) {
    IniParser instance = newInstance();

    instance.setConfig(config);

    return instance;
  }

  /**
   * Parses a stream.
   *
   * <p>The bytes are decoded with the file encoding of the configuration, unless a byte order mark
   * at the start of the stream says otherwise.
   *
   * @param input the stream to read
   * @param handler the receiver of the events
   * @throws IOException if reading fails
   * @throws org.ini4j.InvalidFileFormatException if the content is not valid
   */
  public void parse(InputStream input, IniHandler handler)
      throws IOException, InvalidFileFormatException {
    parse(newIniSource(input, handler), handler);
  }

  /**
   * Parses a reader.
   *
   * @param input the reader to read; the caller has chosen how it decodes
   * @param handler the receiver of the events
   * @throws IOException if reading fails
   * @throws org.ini4j.InvalidFileFormatException if the content is not valid
   */
  public void parse(Reader input, IniHandler handler)
      throws IOException, InvalidFileFormatException {
    parse(newIniSource(input, handler), handler);
  }

  /**
   * Parses the content of a URL.
   *
   * <p>The bytes are decoded as in {@link #parse(InputStream, IniHandler)}. If {@link
   * org.ini4j.Config#isInclude()} is on, relative locations in include lines are resolved against
   * this URL.
   *
   * @param input the location to read
   * @param handler the receiver of the events
   * @throws IOException if the location cannot be opened or read
   * @throws org.ini4j.InvalidFileFormatException if the content is not valid
   */
  public void parse(URL input, IniHandler handler) throws IOException, InvalidFileFormatException {
    parse(newIniSource(input, handler), handler);
  }

  private void parse(IniSource source, IniHandler handler)
      throws IOException, InvalidFileFormatException {
    handler.startIni();
    String sectionName = null;

    for (String line = source.readLine(); line != null; line = source.readLine()) {
      if (line.charAt(0) == SECTION_BEGIN) {
        if (sectionName != null) {
          handler.endSection();
        }

        sectionName = parseSectionLine(line, source, handler);
      } else {
        if (sectionName == null) {
          if (getConfig().isGlobalSection()) {
            sectionName = getConfig().getGlobalSectionName();
            handler.startSection(sectionName);
          } else {
            parseError(line, source.getLineNumber());
          }
        }

        parseOptionLine(line, handler, source.getLineNumber());
      }
    }

    if (sectionName != null) {
      handler.endSection();
    }

    handler.endIni();
  }

  private String parseSectionLine(String line, IniSource source, IniHandler handler)
      throws InvalidFileFormatException {
    String sectionName;

    if (line.charAt(line.length() - 1) != SECTION_END) {
      parseError(line, source.getLineNumber());
    }

    sectionName = unescapeKey(line.substring(1, line.length() - 1).trim());
    if ((sectionName.length() == 0) && !getConfig().isUnnamedSection()) {
      parseError(line, source.getLineNumber());
    }

    if (getConfig().isLowerCaseSection()) {
      sectionName = sectionName.toLowerCase(Locale.getDefault());
    }

    handler.startSection(sectionName);

    return sectionName;
  }
}
