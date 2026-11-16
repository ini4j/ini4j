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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ini4j.spi.IniHandler;
import org.ini4j.spi.Warnings;

/**
 * A reader and writer for INI files that follows the API of the Python {@code ConfigParser} module.
 *
 * <p>It is meant for code that is ported from Python, or that wants methods such as {@link
 * #getInt(String, String)} and {@link #items(String)}. The class is built on an {@link Ini} with
 * its own configuration (a copy of the global one), in which escaping is off, the options of a
 * repeated section name are added to the existing section, a repeated option replaces the earlier
 * value, and the names of sections and options are converted to lower case when a file is read.
 * Names given to the methods are not converted, so use lower case names to find what was read.
 *
 * <p>A section named {@code DEFAULT} (in any letter case) is not a normal section. It does not show
 * up in {@link #sections()}, and {@link #hasSection(String)} is {@code false} for it. Its options
 * are used only to fill in the references of a value.
 *
 * <p>A value can refer to other options with {@code %(name)}. It is replaced by the value of {@code
 * name} in the same section, or, when there is no such option, in the {@code variables} map of the
 * call, in the map given to the constructor, or in the {@code DEFAULT} section, in this order. A
 * reference to a name that none of them has throws an {@link InterpolationMissingOptionException}.
 * A percent sign preceded by a backslash is not a reference, and the backslash stays in the value.
 * Unlike in Python, the reference has no trailing {@code s}. A value that refers to itself,
 * directly or through other options, is not detected: the call does not return.
 *
 * <p>The thread safety of {@link BasicMultiMap} applies.
 *
 * @see Ini
 * @since 0.5.0
 */
public class ConfigParser implements Serializable {
  private static final long serialVersionUID = 9118857036229164353L;
  private PyIni _ini;

  /** Creates a parser with no default values. */
  @SuppressWarnings(Warnings.UNCHECKED)
  public ConfigParser() {
    this(Collections.EMPTY_MAP);
  }

  /**
   * Creates a parser with default values.
   *
   * <p>The map is used as it is, not copied. It supplies values for references in the options (see
   * the description of this class); it does not add options to the sections.
   *
   * @param defaults the default values for references, by option name
   */
  public ConfigParser(Map<String, String> defaults) {
    _ini = new PyIni(defaults);
  }

  /**
   * Returns the value of an option as a boolean, with references resolved.
   *
   * <p>The values {@code 1}, {@code yes}, {@code true} and {@code on} give {@code true}, and {@code
   * 0}, {@code no}, {@code false} and {@code off} give {@code false}, in any letter case.
   *
   * @param section the section name
   * @param option the option name
   * @return the value as a boolean
   * @throws NoSectionException if the section does not exist
   * @throws NoOptionException if the section has no such option
   * @throws InterpolationException if a reference in the value cannot be resolved
   * @throws IllegalArgumentException if the value is none of the accepted words; the message is the
   *     value
   */
  public boolean getBoolean(String section, String option)
      throws NoSectionException, NoOptionException, InterpolationException {
    boolean ret;
    String value = get(section, option);

    if ("1".equalsIgnoreCase(value)
        || "yes".equalsIgnoreCase(value)
        || "true".equalsIgnoreCase(value)
        || "on".equalsIgnoreCase(value)) {
      ret = true;
    } else if ("0".equalsIgnoreCase(value)
        || "no".equalsIgnoreCase(value)
        || "false".equalsIgnoreCase(value)
        || "off".equalsIgnoreCase(value)) {
      ret = false;
    } else {
      throw new IllegalArgumentException(value);
    }

    return ret;
  }

  /**
   * Returns the value of an option as a {@code double}, with references resolved.
   *
   * @param section the section name
   * @param option the option name
   * @return the value as a number
   * @throws NoSectionException if the section does not exist
   * @throws NoOptionException if the section has no such option
   * @throws InterpolationException if a reference in the value cannot be resolved
   * @throws NumberFormatException if the value is not a number
   */
  public double getDouble(String section, String option)
      throws NoSectionException, NoOptionException, InterpolationException {
    return Double.parseDouble(get(section, option));
  }

  /**
   * Returns the value of an option as a {@code float}, with references resolved.
   *
   * @param section the section name
   * @param option the option name
   * @return the value as a number
   * @throws NoSectionException if the section does not exist
   * @throws NoOptionException if the section has no such option
   * @throws InterpolationException if a reference in the value cannot be resolved
   * @throws NumberFormatException if the value is not a number
   */
  public float getFloat(String section, String option)
      throws NoSectionException, NoOptionException, InterpolationException {
    return Float.parseFloat(get(section, option));
  }

  /**
   * Returns the value of an option as an {@code int}, with references resolved.
   *
   * @param section the section name
   * @param option the option name
   * @return the value as a number
   * @throws NoSectionException if the section does not exist
   * @throws NoOptionException if the section has no such option
   * @throws InterpolationException if a reference in the value cannot be resolved
   * @throws NumberFormatException if the value is not an integer
   */
  public int getInt(String section, String option)
      throws NoSectionException, NoOptionException, InterpolationException {
    return Integer.parseInt(get(section, option));
  }

  /**
   * Returns the value of an option as a {@code long}, with references resolved.
   *
   * @param section the section name
   * @param option the option name
   * @return the value as a number
   * @throws NoSectionException if the section does not exist
   * @throws NoOptionException if the section has no such option
   * @throws InterpolationException if a reference in the value cannot be resolved
   * @throws NumberFormatException if the value is not an integer
   */
  public long getLong(String section, String option)
      throws NoSectionException, NoOptionException, InterpolationException {
    return Long.parseLong(get(section, option));
  }

  /**
   * Adds an empty section.
   *
   * @param section the section name, used as it is
   * @throws DuplicateSectionException if a section with this name exists already
   * @throws IllegalArgumentException if the name is {@code DEFAULT} in any letter case
   */
  public void addSection(String section) throws DuplicateSectionException {
    if (_ini.containsKey(section)) {
      throw new DuplicateSectionException(section);
    } else if (PyIni.DEFAULT_SECTION_NAME.equalsIgnoreCase(section)) {
      throw new IllegalArgumentException(section);
    }

    _ini.add(section);
  }

  /**
   * Returns the default values given to the constructor.
   *
   * @return the map, not a copy
   */
  public Map<String, String> defaults() {
    return _ini.getDefaults();
  }

  /**
   * Returns the value of an option, with references resolved.
   *
   * @param section the section name
   * @param option the option name
   * @return the value
   * @throws NoSectionException if the section does not exist
   * @throws NoOptionException if the section has no such option
   * @throws InterpolationException if a reference in the value cannot be resolved
   */
  @SuppressWarnings(Warnings.UNCHECKED)
  public String get(String section, String option)
      throws NoSectionException, NoOptionException, InterpolationException {
    return get(section, option, false, Collections.EMPTY_MAP);
  }

  /**
   * Returns the value of an option, with references resolved only if asked.
   *
   * @param section the section name
   * @param option the option name
   * @param raw if {@code true}, the value is returned as stored, with the references in it
   * @return the value
   * @throws NoSectionException if the section does not exist
   * @throws NoOptionException if the section has no such option
   * @throws InterpolationException if a reference in the value cannot be resolved
   */
  @SuppressWarnings(Warnings.UNCHECKED)
  public String get(String section, String option, boolean raw)
      throws NoSectionException, NoOptionException, InterpolationException {
    return get(section, option, raw, Collections.EMPTY_MAP);
  }

  /**
   * Returns the value of an option, with references resolved only if asked, using extra variables.
   *
   * @param sectionName the section name
   * @param optionName the option name
   * @param raw if {@code true}, the value is returned as stored, with the references in it
   * @param variables values for references that the section does not have, by option name
   * @return the value
   * @throws NoSectionException if the section does not exist
   * @throws NoOptionException if the section has no such option
   * @throws InterpolationException if a reference in the value cannot be resolved
   */
  public String get(
      String sectionName, String optionName, boolean raw, Map<String, String> variables)
      throws NoSectionException, NoOptionException, InterpolationException {
    String value = requireOption(sectionName, optionName);

    if (!raw && (value != null) && (value.indexOf(PyIni.SUBST_CHAR) >= 0)) {
      value = _ini.fetch(sectionName, optionName, variables);
    }

    return value;
  }

  /**
   * Tells whether a section has an option.
   *
   * @param sectionName the section name
   * @param optionName the option name
   * @return {@code true} if the section exists and has the option
   */
  public boolean hasOption(String sectionName, String optionName) {
    Ini.Section section = _ini.get(sectionName);

    return (section != null) && section.containsKey(optionName);
  }

  /**
   * Tells whether a section exists.
   *
   * @param sectionName the section name
   * @return {@code true} if there is such a section; always {@code false} for {@code DEFAULT}
   */
  public boolean hasSection(String sectionName) {
    return _ini.containsKey(sectionName);
  }

  /**
   * Returns the options of a section with their values, with references resolved.
   *
   * @param sectionName the section name
   * @return a new list of entries, in no particular order
   * @throws NoSectionException if the section does not exist
   * @throws InterpolationMissingOptionException if a reference in a value cannot be resolved
   */
  @SuppressWarnings(Warnings.UNCHECKED)
  public List<Map.Entry<String, String>> items(String sectionName)
      throws NoSectionException, InterpolationMissingOptionException {
    return items(sectionName, false, Collections.EMPTY_MAP);
  }

  /**
   * Returns the options of a section with their values, with references resolved only if asked.
   *
   * @param sectionName the section name
   * @param raw if {@code true}, the values are returned as stored
   * @return a new list of entries, in no particular order
   * @throws NoSectionException if the section does not exist
   * @throws InterpolationMissingOptionException if a reference in a value cannot be resolved
   */
  @SuppressWarnings(Warnings.UNCHECKED)
  public List<Map.Entry<String, String>> items(String sectionName, boolean raw)
      throws NoSectionException, InterpolationMissingOptionException {
    return items(sectionName, raw, Collections.EMPTY_MAP);
  }

  /**
   * Returns the options of a section with their values, using extra variables.
   *
   * <p>Changing an entry of the returned list does not change the parser.
   *
   * @param sectionName the section name
   * @param raw if {@code true}, the values are returned as stored
   * @param variables values for references that the section does not have, by option name
   * @return a new list of entries, in no particular order
   * @throws NoSectionException if the section does not exist
   * @throws InterpolationMissingOptionException if a reference in a value cannot be resolved
   */
  public List<Map.Entry<String, String>> items(
      String sectionName, boolean raw, Map<String, String> variables)
      throws NoSectionException, InterpolationMissingOptionException {
    Ini.Section section = requireSection(sectionName);
    Map<String, String> ret;

    if (raw) {
      ret = new HashMap<String, String>(section);
    } else {
      ret = new HashMap<String, String>();
      for (String key : section.keySet()) {
        ret.put(key, _ini.fetch(section, key, variables));
      }
    }

    return new ArrayList<Map.Entry<String, String>>(ret.entrySet());
  }

  /**
   * Returns the option names of a section.
   *
   * @param sectionName the section name
   * @return a new list of names, in the order of the section
   * @throws NoSectionException if the section does not exist
   */
  public List<String> options(String sectionName) throws NoSectionException {
    requireSection(sectionName);

    return new ArrayList<String>(_ini.get(sectionName).keySet());
  }

  /**
   * Reads files.
   *
   * <p>The files are read one after the other with {@link #read(File)}. A later file adds to what
   * was read before and replaces options that are in both.
   *
   * @param filenames the names of the files
   * @throws IOException if a file cannot be read
   * @throws ParsingException if a file is not valid
   */
  public void read(String... filenames) throws IOException, ParsingException {
    for (String filename : filenames) {
      read(new File(filename));
    }
  }

  /**
   * Reads from a reader.
   *
   * <p>The content is added to what the parser holds, and replaces options that are in both.
   *
   * @param reader the reader to read
   * @throws IOException if reading fails
   * @throws ParsingException if the content is not valid
   */
  public void read(Reader reader) throws IOException, ParsingException {
    try {
      _ini.load(reader);
    } catch (InvalidFileFormatException x) {
      throw new ParsingException(x);
    }
  }

  /**
   * Reads from a URL.
   *
   * <p>The content is decoded with the file encoding that the global configuration had when this
   * parser was created, unless a byte order mark at the start says otherwise.
   *
   * @param url the location to read
   * @throws IOException if reading fails
   * @throws ParsingException if the content is not valid
   */
  public void read(URL url) throws IOException, ParsingException {
    try {
      _ini.load(url);
    } catch (InvalidFileFormatException x) {
      throw new ParsingException(x);
    }
  }

  /**
   * Reads a file.
   *
   * <p>The file is decoded with the file encoding of {@link Config#getGlobal()} at the time of the
   * call. A byte order mark is not detected.
   *
   * @param file the file to read
   * @throws IOException if the file cannot be read
   * @throws ParsingException if the content is not valid
   */
  public void read(File file) throws IOException, ParsingException {
    try {
      FileReader reader = new FileReader(file, Config.getGlobal().getFileEncoding());
      try {
        _ini.load(reader);
      } finally {
        reader.close();
      }
    } catch (InvalidFileFormatException x) {
      throw new ParsingException(x);
    }
  }

  /**
   * Reads from a stream.
   *
   * <p>The content is decoded with the file encoding that the global configuration had when this
   * parser was created. A byte order mark is not detected.
   *
   * @param stream the stream to read
   * @throws IOException if reading fails
   * @throws ParsingException if the content is not valid
   */
  public void read(InputStream stream) throws IOException, ParsingException {
    try {
      _ini.load(stream);
    } catch (InvalidFileFormatException x) {
      throw new ParsingException(x);
    }
  }

  /**
   * Removes an option from a section.
   *
   * @param sectionName the section name
   * @param optionName the option name
   * @return {@code true} if the option existed
   * @throws NoSectionException if the section does not exist
   */
  public boolean removeOption(String sectionName, String optionName) throws NoSectionException {
    Ini.Section section = requireSection(sectionName);
    boolean ret = section.containsKey(optionName);

    section.remove(optionName);

    return ret;
  }

  /**
   * Removes a section.
   *
   * @param sectionName the section name
   * @return {@code true} if the section existed
   */
  public boolean removeSection(String sectionName) {
    boolean ret = _ini.containsKey(sectionName);

    _ini.remove(sectionName);

    return ret;
  }

  /**
   * Returns the section names.
   *
   * @return a new list of names, in the order of the sections, without {@code DEFAULT}
   */
  public List<String> sections() {
    return new ArrayList<String>(_ini.keySet());
  }

  /**
   * Sets the value of an option.
   *
   * <p>The option name is used as it is, not converted to lower case.
   *
   * @param sectionName the section name
   * @param optionName the option name
   * @param value the new value, stored as {@link Object#toString()}; with {@code null} the option
   *     is removed
   * @throws NoSectionException if the section does not exist
   */
  public void set(String sectionName, String optionName, Object value) throws NoSectionException {
    Ini.Section section = requireSection(sectionName);

    if (value == null) {
      section.remove(optionName);
    } else {
      section.put(optionName, value.toString());
    }
  }

  /**
   * Writes the sections and options.
   *
   * <p>The {@code DEFAULT} section comes first. Comments are not written.
   *
   * @param writer the writer to write
   * @throws IOException if writing fails
   */
  public void write(Writer writer) throws IOException {
    _ini.store(writer);
  }

  /**
   * Writes the sections and options to a stream.
   *
   * <p>The content is the same as in {@link #write(Writer)}. It is encoded with the file encoding
   * that the global configuration had when this parser was created.
   *
   * @param stream the stream to write
   * @throws IOException if writing fails
   */
  public void write(OutputStream stream) throws IOException {
    _ini.store(stream);
  }

  /**
   * Writes the sections and options to a file.
   *
   * <p>The content is the same as in {@link #write(Writer)}. The file is created or overwritten,
   * and encoded with the file encoding of {@link Config#getGlobal()}.
   *
   * @param file the file to write
   * @throws IOException if the file cannot be written
   */
  public void write(File file) throws IOException {
    FileWriter writer = new FileWriter(file, Config.getGlobal().getFileEncoding());
    try {
      _ini.store(writer);
    } finally {
      writer.close();
    }
  }

  /**
   * Returns the underlying {@link Ini}.
   *
   * @return the object that holds the sections and options
   */
  protected Ini getIni() {
    return _ini;
  }

  private String requireOption(String sectionName, String optionName)
      throws NoSectionException, NoOptionException {
    Ini.Section section = requireSection(sectionName);
    String option = section.get(optionName);

    if (option == null) {
      throw new NoOptionException(optionName);
    }

    return option;
  }

  private Ini.Section requireSection(String sectionName) throws NoSectionException {
    Ini.Section section = _ini.get(sectionName);

    if (section == null) {
      throw new NoSectionException(sectionName);
    }

    return section;
  }

  /**
   * The base class of the checked exceptions of {@link ConfigParser}.
   *
   * @since 0.5.0
   */
  public static class ConfigParserException extends Exception {

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -6845546313519392093L;

    /**
     * Creates an exception with a detail message.
     *
     * @param message the detail message
     */
    public ConfigParserException(String message) {
      super(message);
    }
  }

  /**
   * Signals that {@link ConfigParser#addSection(String)} was called for a section that exists. The
   * message is the section name.
   *
   * @since 0.5.0
   */
  public static final class DuplicateSectionException extends ConfigParserException {

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -5244008445735700699L;

    private DuplicateSectionException(String message) {
      super(message);
    }
  }

  /**
   * The base class of the exceptions for a value that refers to other options.
   *
   * @since 0.5.0
   */
  public static class InterpolationException extends ConfigParserException {

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 8924443303158546939L;

    /**
     * Creates an exception with a detail message.
     *
     * @param message the detail message
     */
    protected InterpolationException(String message) {
      super(message);
    }
  }

  /**
   * Signals that a reference in a value names an option that cannot be found. The message is the
   * option name.
   *
   * @since 0.5.0
   */
  public static final class InterpolationMissingOptionException extends InterpolationException {

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 2903136975820447879L;

    private InterpolationMissingOptionException(String message) {
      super(message);
    }
  }

  /**
   * Signals that a section has no option of the requested name. The message is the option name.
   *
   * @since 0.5.0
   */
  public static final class NoOptionException extends ConfigParserException {

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 8460082078809425858L;

    private NoOptionException(String message) {
      super(message);
    }
  }

  /**
   * Signals that there is no section of the requested name. The message is the section name.
   *
   * @since 0.5.0
   */
  public static final class NoSectionException extends ConfigParserException {

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 8553627727493146118L;

    private NoSectionException(String message) {
      super(message);
    }
  }

  /**
   * Signals that a file read by a {@link ConfigParser} is not valid.
   *
   * <p>The message and the cause are those of the {@link InvalidFileFormatException} that the
   * parser raised.
   *
   * @since 0.5.0
   */
  public static final class ParsingException extends IOException {

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -5395990242007205038L;

    private ParsingException(Throwable cause) {
      super(cause.getMessage());
      initCause(cause);
    }
  }

  static class PyIni extends Ini {
    private static final char SUBST_CHAR = '%';
    private static final Pattern EXPRESSION = Pattern.compile("(?<!\\\\)\\%\\(([^\\)]+)\\)");
    private static final int G_OPTION = 1;
    protected static final String DEFAULT_SECTION_NAME = "DEFAULT";
    private static final long serialVersionUID = -7152857626328996122L;
    private final Map<String, String> _defaults;
    private Ini.Section _defaultSection;

    public PyIni(Map<String, String> defaults) {
      _defaults = defaults;
      Config cfg = getConfig().clone();

      cfg.setEscape(false);
      cfg.setMultiOption(false);
      cfg.setMultiSection(false);
      cfg.setLowerCaseOption(true);
      cfg.setLowerCaseSection(true);
      super.setConfig(cfg);
    }

    @Override
    public void setConfig(Config value) {
      assert true;
    }

    public Map<String, String> getDefaults() {
      return _defaults;
    }

    @Override
    public Section add(String name) {
      Section section;

      if (DEFAULT_SECTION_NAME.equalsIgnoreCase(name)) {
        if (_defaultSection == null) {
          _defaultSection = newSection(name);
        }

        section = _defaultSection;
      } else {
        section = super.add(name);
      }

      return section;
    }

    public String fetch(String sectionName, String optionName, Map<String, String> variables)
        throws InterpolationMissingOptionException {
      return fetch(get(sectionName), optionName, variables);
    }

    protected Ini.Section getDefaultSection() {
      return _defaultSection;
    }

    protected String fetch(Ini.Section section, String optionName, Map<String, String> variables)
        throws InterpolationMissingOptionException {
      String value = section.get(optionName);

      if ((value != null) && (value.indexOf(SUBST_CHAR) >= 0)) {
        StringBuilder buffer = new StringBuilder(value);

        resolve(buffer, section, variables);
        value = buffer.toString();
      }

      return value;
    }

    protected void resolve(StringBuilder buffer, Ini.Section owner, Map<String, String> vars)
        throws InterpolationMissingOptionException {
      Matcher m = EXPRESSION.matcher(buffer);

      while (m.find()) {
        String optionName = m.group(G_OPTION);
        String value = owner.get(optionName);

        if (value == null) {
          value = vars.get(optionName);
        }

        if (value == null) {
          value = _defaults.get(optionName);
        }

        if ((value == null) && (_defaultSection != null)) {
          value = _defaultSection.get(optionName);
        }

        if (value == null) {
          throw new InterpolationMissingOptionException(optionName);
        }

        buffer.replace(m.start(), m.end(), value);
        m.reset(buffer);
      }
    }

    @Override
    protected void store(IniHandler formatter) {
      formatter.startIni();
      if (_defaultSection != null) {
        store(formatter, _defaultSection);
      }

      for (Ini.Section s : values()) {
        store(formatter, s);
      }

      formatter.endIni();
    }

    @Override
    protected void store(IniHandler formatter, Section section) {
      formatter.startSection(section.getName());
      for (String name : section.keySet()) {
        formatter.handleOption(name, section.get(name));
      }

      formatter.endSection();
    }
  }
}
