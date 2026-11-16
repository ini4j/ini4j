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

import java.io.IOException;

/**
 * Signals that the content being loaded is not valid for the format.
 *
 * <p>The parsers throw it, for example, for a line that is neither a section header nor an option,
 * or for a registry file with a missing or unsupported version header. The message of a parse error
 * names the line number and the line. It is an {@link IOException}, so the {@code load} methods of
 * {@link Persistable} declare it without a new checked type.
 *
 * @see Persistable#load()
 * @since 0.5.0
 */
public class InvalidFileFormatException extends IOException {
  private static final long serialVersionUID = -4354616830804732309L;

  /**
   * Constructs an exception with a detail message.
   *
   * @param message the detail message
   */
  public InvalidFileFormatException(String message) {
    super(message);
  }
}
