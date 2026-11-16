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
import java.io.Writer;
import org.ini4j.Config;

/**
 * Writes the events of an INI file as text.
 *
 * <p>Each section is written as a header {@code [name]} followed by its options, and an empty line
 * ends the section. If {@link org.ini4j.Config#isGlobalSection()} is on, the section that has the
 * name {@link org.ini4j.Config#getGlobalSectionName()} has no header. Section names are escaped
 * like option names. Comments and options are written as described in {@link
 * #handleComment(String)} and {@link #handleOption(String, String)}. The writer is flushed at the
 * end of the file and is never closed.
 *
 * @see IniHandler
 * @see IniParser
 * @since 0.4.1
 */
public class IniFormatter extends AbstractFormatter implements IniHandler {
  /**
   * Creates a formatter of this class, or of the class that a provider names.
   *
   * <p>The lookup is the same as in {@link IniParser#newInstance()}, with the name {@code
   * org.ini4j.spi.IniFormatter}.
   *
   * @param out the writer for the text; it is used directly if it is a {@link java.io.PrintWriter},
   *     and wrapped in one otherwise
   * @param config the configuration to use
   * @return a new formatter
   * @throws IllegalArgumentException if the provider cannot be found or created
   */
  public static IniFormatter newInstance(Writer out, Config config) {
    IniFormatter instance = newInstance();

    instance.setOutput((out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out));
    instance.setConfig(config);

    return instance;
  }

  /** Flushes the writer. */
  @Override
  public void endIni() {
    getOutput().flush();
  }

  /** Writes an empty line. */
  @Override
  public void endSection() {
    getOutput().print(getConfig().getLineSeparator());
  }

  /** Does nothing. */
  @Override
  public void startIni() {
    assert true;
  }

  /**
   * Writes the header of a section, unless it is the global section.
   *
   * @param sectionName the name of the section
   */
  @Override
  public void startSection(String sectionName) {
    setHeader(false);
    if (!getConfig().isGlobalSection() || !sectionName.equals(getConfig().getGlobalSectionName())) {
      getOutput().print(IniParser.SECTION_BEGIN);
      getOutput().print(escapeKey(sectionName));
      getOutput().print(IniParser.SECTION_END);
      getOutput().print(getConfig().getLineSeparator());
    }
  }

  private static IniFormatter newInstance() {
    return ServiceFinder.findService(IniFormatter.class);
  }
}
