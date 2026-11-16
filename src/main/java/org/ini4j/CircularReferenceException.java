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
 * Signals that variable substitution went deeper than the allowed depth.
 *
 * <p>The variables in a value can refer to options whose values contain variables again, and the
 * resolver follows them one level at a time. The exception is thrown when the nesting reaches the
 * limit set by {@link Config#getMaxResolveDepth()}. The usual cause is a circular reference, such
 * as:
 *
 * <pre>
 * [section1]
 * a = ${section2/b}
 *
 * [section2]
 * b = ${section1/a}
 * </pre>
 *
 * <p>The limit also stops a very deep chain of references that is not circular. It is an {@link
 * IllegalArgumentException}, so it is unchecked. The limit prevents the {@link StackOverflowError}
 * that unbounded recursion would cause.
 *
 * <p>The limit was added to fix CVE-2022-41404, a denial of service through circular variable
 * references.
 *
 * @see Config#getMaxResolveDepth()
 * @see Config#setMaxResolveDepth(int)
 * @see OptionMap#fetch(Object)
 * @since 0.6.0
 */
public class CircularReferenceException extends IllegalArgumentException {
  private static final long serialVersionUID = 1L;

  /**
   * Creates an exception with a standard message that names the depth limit.
   *
   * @param maxResolveDepth the depth limit that was reached
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
   * Creates an exception with a detail message.
   *
   * @param message the detail message
   */
  public CircularReferenceException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a detail message and a cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public CircularReferenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
