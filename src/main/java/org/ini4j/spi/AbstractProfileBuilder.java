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

import org.ini4j.CommentedMap;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;

/**
 * The common part of the builders that fill a {@link org.ini4j.Profile} from the events of a
 * parser.
 *
 * <p>The rules are described in {@link IniBuilder}.
 *
 * @since 0.5.0
 */
abstract class AbstractProfileBuilder implements IniHandler {
  private Profile.Section _currentSection;
  private boolean _header;
  private String _lastComment;

  /**
   * Ends the file. A comment that is the only content of the file becomes the comment of the
   * profile, if {@link org.ini4j.Config#isComment()} and {@link org.ini4j.Config#isHeaderComment()}
   * are on. The rules are described in {@link IniBuilder}.
   */
  @Override
  public void endIni() {

    // a file that has nothing but a comment
    if ((_lastComment != null) && _header) {
      setHeaderComment();
    }
  }

  /** Ends the current section. */
  @Override
  public void endSection() {
    _currentSection = null;
  }

  /**
   * Receives a comment.
   *
   * @param comment the text of the comment
   */
  @Override
  public void handleComment(String comment) {
    if ((_lastComment != null) && _header) {
      _header = false;
      setHeaderComment();
    }

    _lastComment = comment;
  }

  /**
   * Adds an option to the current section.
   *
   * <p>The value is added or replaces the last value as {@link org.ini4j.Config#isMultiOption()}
   * says. A held comment becomes the comment of the option.
   *
   * @param name the name of the option
   * @param value the value of the option, or {@code null}
   * @throws NullPointerException if there is no current section
   */
  @Override
  public void handleOption(String name, String value) {
    _header = false;
    if (getConfig().isMultiOption()) {
      _currentSection.add(name, value);
    } else {
      _currentSection.put(name, value);
    }

    if (_lastComment != null) {
      putComment(_currentSection, name);
      _lastComment = null;
    }
  }

  /** Starts the file. */
  @Override
  public void startIni() {
    if (getConfig().isHeaderComment()) {
      _header = true;
    }
  }

  /**
   * Starts a section.
   *
   * <p>The section is added to the profile, or an existing one is reused as {@link
   * org.ini4j.Config#isMultiSection()} says. A held comment becomes the comment of the section, or
   * of the profile if no section or option came before it and header comments are on.
   *
   * @param sectionName the name of the section
   */
  @Override
  public void startSection(String sectionName) {
    if (getConfig().isMultiSection()) {
      _currentSection = getProfile().add(sectionName);
    } else {
      Ini.Section s = getProfile().get(sectionName);

      _currentSection = (s == null) ? getProfile().add(sectionName) : s;
    }

    if (_lastComment != null) {
      if (_header) {
        setHeaderComment();
      } else {
        putComment(getProfile(), sectionName);
      }

      _lastComment = null;
    }

    _header = false;
  }

  abstract Config getConfig();

  abstract Profile getProfile();

  Profile.Section getCurrentSection() {
    return _currentSection;
  }

  private void setHeaderComment() {
    if (getConfig().isComment()) {
      getProfile().setComment(_lastComment);
    }
  }

  private void putComment(CommentedMap<String, ?> map, String key) {
    if (getConfig().isComment()) {
      map.putComment(key, _lastComment);
    }
  }
}
