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
import org.ini4j.Profile;
import org.ini4j.Reg;
import org.ini4j.Registry.Key;
import org.ini4j.Registry.Type;

/**
 * Fills a {@link org.ini4j.Reg} from the events of an {@link IniParser}.
 *
 * <p>Sections, options and comments are handled as described in {@link IniBuilder}. In addition,
 * each option is a registry value. A name in double quotes is unquoted, and the name {@code @} is
 * kept as it is. The raw value is decoded by {@link RegEscapeTool}: the value types that are not
 * {@link org.ini4j.Registry.Type#REG_SZ} are remembered in the key with {@link
 * org.ini4j.Registry.Key#putType(String, org.ini4j.Registry.Type)}, and a multi-string value gives
 * one value for each string, or only the last string if {@link org.ini4j.Config#isMultiOption()} is
 * off. A comment before a value is the comment of its name.
 *
 * @see org.ini4j.Reg
 * @since 0.5.0
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP2")
public class RegBuilder extends AbstractProfileBuilder {
  private Reg _reg;

  /**
   * Creates a builder of this class, or of the class that a provider names, for a {@code Reg}.
   *
   * <p>The lookup is the same as in {@link IniParser#newInstance()}, with the name {@code
   * org.ini4j.spi.RegBuilder}.
   *
   * @param reg the object to fill; it is used directly, not copied
   * @return a new builder
   * @throws IllegalArgumentException if the provider cannot be found or created
   */
  public static RegBuilder newInstance(Reg reg) {
    RegBuilder instance = newInstance();

    instance.setReg(reg);

    return instance;
  }

  /**
   * Sets the object to fill.
   *
   * @param value the object to fill; it is used directly, not copied
   */
  public void setReg(Reg value) {
    _reg = value;
  }

  /**
   * Adds a registry value to the current key.
   *
   * @param rawName the name as written in the file, in double quotes unless it is {@code @}
   * @param rawValue the value as written in the file, with its type prefix if it has one
   * @throws NullPointerException if there is no current key
   * @throws StringIndexOutOfBoundsException if {@code rawName} or {@code rawValue} is empty
   * @throws IllegalArgumentException if the value cannot be decoded, for example a {@code dword}
   *     that is not a hexadecimal number
   */
  @Override
  public void handleOption(String rawName, String rawValue) {
    String name =
        (rawName.charAt(0) == EscapeTool.DOUBLE_QUOTE)
            ? RegEscapeTool.getInstance().unquote(rawName)
            : rawName;
    TypeValuesPair tv = RegEscapeTool.getInstance().decode(rawValue);

    if (tv.getType() != Type.REG_SZ) {
      ((Key) getCurrentSection()).putType(name, tv.getType());
    }

    for (String value : tv.getValues()) {
      super.handleOption(name, value);
    }
  }

  @Override
  Config getConfig() {
    return _reg.getConfig();
  }

  @Override
  Profile getProfile() {
    return _reg;
  }

  private static RegBuilder newInstance() {
    return ServiceFinder.findService(RegBuilder.class);
  }
}
