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
import org.ini4j.Options;

/**
 * Fills an {@link org.ini4j.Options} from the events of an {@link OptionsParser}.
 *
 * <p>{@code handleOption} adds the value, or replaces the last value if {@link
 * org.ini4j.Config#isMultiOption()} is off. Comments are kept only if {@link
 * org.ini4j.Config#isComment()} is on, as follows. A comment is held until the next option and
 * becomes the comment of that option. If another comment arrives first, it replaces the held one,
 * which is lost. The exception is the first comment of the file: with {@link
 * org.ini4j.Config#isHeaderComment()} on, the first comment before the first option is not lost but
 * becomes the comment of the {@code Options} itself, whether it is followed by an option, another
 * comment, or the end of the file. A comment that is not followed by an option, such as a comment
 * at the end of the file, is lost.
 *
 * @see OptionsParser
 * @since 0.5.0
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP2")
public class OptionsBuilder implements OptionsHandler {
  private boolean _header;
  private String _lastComment;
  private Options _options;

  /**
   * Creates a builder of this class, or of the class that a provider names, for an {@code Options}.
   *
   * <p>The lookup is the same as in {@link IniParser#newInstance()}, with the name {@code
   * org.ini4j.spi.OptionsBuilder}.
   *
   * @param opts the object to fill; it is used directly, not copied
   * @return a new builder
   * @throws IllegalArgumentException if the provider cannot be found or created
   */
  public static OptionsBuilder newInstance(Options opts) {
    OptionsBuilder instance = newInstance();

    instance.setOptions(opts);

    return instance;
  }

  /**
   * Sets the object to fill.
   *
   * @param value the object to fill; it is used directly, not copied
   */
  public void setOptions(Options value) {
    _options = value;
  }

  /**
   * Ends the file. A comment that is the only content of the file becomes the comment of the {@code
   * Options}, if {@link org.ini4j.Config#isComment()} and {@link
   * org.ini4j.Config#isHeaderComment()} are on.
   */
  @Override
  public void endOptions() {

    // a file that has nothing but a comment
    if ((_lastComment != null) && _header) {
      setHeaderComment();
    }
  }

  /**
   * Receives a comment.
   *
   * @param comment the text of the comment
   */
  @Override
  public void handleComment(String comment) {
    if ((_lastComment != null) && _header) {
      setHeaderComment();
      _header = false;
    }

    _lastComment = comment;
  }

  /**
   * Adds an option.
   *
   * @param name the name of the option
   * @param value the value of the option, or {@code null}
   */
  @Override
  public void handleOption(String name, String value) {
    if (getConfig().isMultiOption()) {
      _options.add(name, value);
    } else {
      _options.put(name, value);
    }

    if (_lastComment != null) {
      if (_header) {
        setHeaderComment();
      } else {
        putComment(name);
      }

      _lastComment = null;
    }

    _header = false;
  }

  /** Starts the file. */
  @Override
  public void startOptions() {
    if (getConfig().isHeaderComment()) {
      _header = true;
    }
  }

  /**
   * Creates a builder of this class, or of the class that a provider names, without an object to
   * fill.
   *
   * <p>The lookup is the same as in {@link IniParser#newInstance()}, with the name {@code
   * org.ini4j.spi.OptionsBuilder}. Call {@link #setOptions(Options)} before the builder receives
   * events.
   *
   * @return a new builder
   * @throws IllegalArgumentException if the provider cannot be found or created
   */
  protected static OptionsBuilder newInstance() {
    return ServiceFinder.findService(OptionsBuilder.class);
  }

  private Config getConfig() {
    return _options.getConfig();
  }

  private void setHeaderComment() {
    if (getConfig().isComment()) {
      _options.setComment(_lastComment);
    }
  }

  private void putComment(String key) {
    if (getConfig().isComment()) {
      _options.putComment(key, _lastComment);
    }
  }
}
