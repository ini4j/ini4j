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

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;

/**
 * Fills an {@link org.ini4j.Ini} from the events of an {@link IniParser}.
 *
 * <p>The builder receives the events described in {@link IniHandler}. {@code startSection} adds a
 * section to the {@code Ini}, or, if {@link org.ini4j.Config#isMultiSection()} is off and a section
 * of that name exists, selects the existing one. {@code handleOption} adds the value to the current
 * section, or replaces the last value if {@link org.ini4j.Config#isMultiOption()} is off. It must
 * be called between {@code startSection} and {@code endSection}. The configuration is that of the
 * {@code Ini}.
 *
 * <p>Comments are kept only if {@link org.ini4j.Config#isComment()} is on. A comment is held until
 * the next section or option, and becomes the comment of that section or option. If another comment
 * arrives first, it replaces the held one, which is lost. The exception is the first comment of the
 * file: with {@link org.ini4j.Config#isHeaderComment()} on, the first comment before any section or
 * option is not lost but becomes the comment of the {@code Ini} itself, whether it is followed by a
 * section, another comment, or the end of the file. A comment that is not followed by a section or
 * an option, such as the comments at the end of the file, is lost.
 *
 * <p>{@link RegBuilder} follows the same rules.
 *
 * @see IniParser
 * @since 0.5.0
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP2")
public class IniBuilder extends AbstractProfileBuilder {
  private Ini _ini;

  /**
   * Creates a builder of this class, or of the class that a provider names, for an {@code Ini}.
   *
   * <p>The lookup is the same as in {@link IniParser#newInstance()}, with the name {@code
   * org.ini4j.spi.IniBuilder}.
   *
   * @param ini the object to fill; it is used directly, not copied
   * @return a new builder
   * @throws IllegalArgumentException if the provider cannot be found or created
   */
  public static IniBuilder newInstance(Ini ini) {
    IniBuilder instance = newInstance();

    instance.setIni(ini);

    return instance;
  }

  /**
   * Sets the object to fill.
   *
   * @param value the object to fill; it is used directly, not copied
   */
  public void setIni(Ini value) {
    _ini = value;
  }

  @Override
  Config getConfig() {
    return _ini.getConfig();
  }

  @Override
  Profile getProfile() {
    return _ini;
  }

  private static IniBuilder newInstance() {
    return ServiceFinder.findService(IniBuilder.class);
  }
}
