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
import org.ini4j.Config;
import org.ini4j.InvalidFileFormatException;

/**
 * Parses files of options without sections and sends the events to an {@link OptionsHandler}.
 *
 * <p>Every line is an option, written as {@code name=value} or {@code name:value}. A line that
 * starts with {@code !} or {@code #} is a comment. Blank lines are skipped and lines are trimmed.
 * The rules for names, values and the {@link org.ini4j.Config} flags are the same as in {@link
 * IniParser}, except that there are no sections.
 *
 * <p>The events and their order are described in {@link OptionsHandler}. A line that is not valid
 * throws an {@link org.ini4j.InvalidFileFormatException} whose message has the line number and the
 * line. The input is closed when it has been read to the end. If an error is found before that, it
 * is left open.
 *
 * @see OptionsHandler
 * @see OptionsFormatter
 * @since 0.5.0
 */
public class OptionsParser extends AbstractParser {
  private static final String COMMENTS = "!#";
  private static final String OPERATORS = ":=";

  /**
   * Creates a parser that uses {@link org.ini4j.Config#getGlobal()}.
   *
   * <p>Use {@link #newInstance()} or {@link #newInstance(org.ini4j.Config)} unless you want to
   * bypass the provider lookup.
   */
  public OptionsParser() {
    super(OPERATORS, COMMENTS);
  }

  /**
   * Creates a parser of this class, or of the class that a provider names.
   *
   * <p>The lookup is the same as in {@link IniParser#newInstance()}, with the name {@code
   * org.ini4j.spi.OptionsParser}.
   *
   * @return a new parser
   * @throws IllegalArgumentException if the provider cannot be found or created
   */
  public static OptionsParser newInstance() {
    return ServiceFinder.findService(OptionsParser.class);
  }

  /**
   * Creates a parser as {@link #newInstance()} does, and gives it a configuration.
   *
   * @param config the configuration to use
   * @return a new parser
   * @throws IllegalArgumentException if the provider cannot be found or created
   */
  public static OptionsParser newInstance(Config config) {
    OptionsParser instance = newInstance();

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
  public void parse(InputStream input, OptionsHandler handler)
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
  public void parse(Reader input, OptionsHandler handler)
      throws IOException, InvalidFileFormatException {
    parse(newIniSource(input, handler), handler);
  }

  /**
   * Parses the content of a URL.
   *
   * <p>The bytes are decoded as in {@link #parse(InputStream, OptionsHandler)}. If {@link
   * org.ini4j.Config#isInclude()} is on, relative locations in include lines are resolved against
   * this URL.
   *
   * @param input the location to read
   * @param handler the receiver of the events
   * @throws IOException if the location cannot be opened or read
   * @throws org.ini4j.InvalidFileFormatException if the content is not valid
   */
  public void parse(URL input, OptionsHandler handler)
      throws IOException, InvalidFileFormatException {
    parse(newIniSource(input, handler), handler);
  }

  private void parse(IniSource source, OptionsHandler handler)
      throws IOException, InvalidFileFormatException {
    handler.startOptions();
    for (String line = source.readLine(); line != null; line = source.readLine()) {
      parseOptionLine(line, handler, source.getLineNumber());
    }

    handler.endOptions();
  }
}
