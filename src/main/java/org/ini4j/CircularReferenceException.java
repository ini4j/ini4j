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

/**
 * Exception thrown when a circular reference is detected during variable resolution.
 *
 * <p>This exception is thrown when the maximum recursion depth is exceeded while resolving variable
 * substitutions in INI files or option maps. This typically indicates a circular reference in the
 * configuration, such as:
 *
 * <pre>
 * [section1]
 * a = ${section2/b}
 *
 * [section2]
 * b = ${section1/a}
 * </pre>
 *
 * <p>This exception prevents {@link StackOverflowError} that would otherwise occur from infinite
 * recursion.
 *
 * <p><b>Security Note:</b> This exception is part of the fix for CVE-2022-41404, which addressed a
 * Denial of Service vulnerability where circular variable references could crash the JVM.
 *
 * @see Config#getMaxResolveDepth()
 * @see Config#setMaxResolveDepth(int)
 * @since 0.6.0
 */
public class CircularReferenceException extends IllegalArgumentException {
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new circular reference exception with the specified maximum resolve depth.
   *
   * @param maxResolveDepth the maximum recursion depth that was exceeded
   */
  public CircularReferenceException(int maxResolveDepth) {
    super(
        "Variable resolution depth limit ("
            + maxResolveDepth
            + ") exceeded. "
            + "Circular reference detected in variable substitution. "
            + "Check for circular dependencies in variable references. "
            + "Increase org.ini4j.config.maxResolveDepth if legitimate deep nesting is needed.");
  }

  /**
   * Constructs a new circular reference exception with the specified detail message.
   *
   * @param message the detail message
   */
  public CircularReferenceException(String message) {
    super(message);
  }

  /**
   * Constructs a new circular reference exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public CircularReferenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
