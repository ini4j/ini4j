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
 * Writes the events of a file of options as text.
 *
 * <p>Comments and options are written as described in {@link #handleComment(String)} and {@link
 * #handleOption(String, String)}. The writer is flushed at the end of the file and is never closed.
 *
 * @see OptionsHandler
 * @see OptionsParser
 * @since 0.5.0
 */
public class OptionsFormatter extends AbstractFormatter implements OptionsHandler {
  /**
   * Creates a formatter of this class, or of the class that a provider names.
   *
   * <p>The lookup is the same as in {@link IniParser#newInstance()}, with the name {@code
   * org.ini4j.spi.OptionsFormatter}.
   *
   * @param out the writer for the text; it is used directly if it is a {@link java.io.PrintWriter},
   *     and wrapped in one otherwise
   * @param config the configuration to use
   * @return a new formatter
   * @throws IllegalArgumentException if the provider cannot be found or created
   */
  public static OptionsFormatter newInstance(Writer out, Config config) {
    OptionsFormatter instance = newInstance();

    instance.setOutput((out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out));
    instance.setConfig(config);

    return instance;
  }

  /** Flushes the writer. */
  public void endOptions() {
    getOutput().flush();
  }

  /** Does nothing. */
  public void startOptions() {
    assert true;
  }

  private static OptionsFormatter newInstance() {
    return ServiceFinder.findService(OptionsFormatter.class);
  }
}
