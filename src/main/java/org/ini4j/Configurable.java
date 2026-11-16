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
 * An object whose parsing and formatting behavior is set by a {@link Config}.
 *
 * <p>{@link Ini}, {@link Options} and {@link Reg} are configurable. {@link Ini} starts with the
 * {@linkplain Config#getGlobal() global} configuration itself, which other objects share, so a
 * change to it affects all of them. {@link Options} and {@link Reg} start with their own copy of
 * the global configuration with some flags set for their format. Call {@link #setConfig(Config)}
 * with a {@linkplain Config#clone() clone} to change the settings of one object only.
 *
 * @see Config
 * @since 0.5.0
 */
public interface Configurable {
  /**
   * Returns the configuration.
   *
   * @return the configuration in use
   */
  Config getConfig();

  /**
   * Sets the configuration.
   *
   * @param value the configuration to use from now on
   */
  void setConfig(Config value);
}
