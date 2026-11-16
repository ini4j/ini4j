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
 * The events that the parsers and the formatters of both file formats share.
 *
 * <p>A parser calls these methods while it reads a file, and a profile calls them while it is
 * stored. The formats add their own start and end events: see {@link IniHandler} and {@link
 * OptionsHandler}.
 *
 * @since 0.5.0
 */
interface HandlerBase {
  /**
   * Receives a comment.
   *
   * @param comment the text of the comment, see the description of the subinterface
   */
  void handleComment(String comment);

  /**
   * Receives an option.
   *
   * @param optionName the name of the option
   * @param optionValue the value of the option
   */
  void handleOption(String optionName, String optionValue);
}
