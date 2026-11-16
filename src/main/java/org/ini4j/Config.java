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

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The settings that control how files are read and written.
 *
 * <p>A configuration is a set of flags and a few other values, such as the file encoding and the
 * line separator. They decide, for example, whether an option can have several values, whether
 * comments are kept and how special characters are escaped. {@link Ini}, {@link Options} and {@link
 * Reg} are {@link Configurable}, and the parsers and formatters of the {@code org.ini4j.spi}
 * package read it. Each option is described once, at its getter. The matching setter, the {@code
 * PROP_} constant (the name of the system property) and the {@code DEFAULT_} constant point to it.
 *
 * <p>The initial value of an option is read from a system property whose name is {@link
 * #KEY_PREFIX} followed by the {@code PROP_} constant, for example {@code
 * -Dorg.ini4j.config.multiSection=true}. If the property is not set, the {@code DEFAULT_} constant
 * is used. The properties are read when a {@code Config} is created and again by {@link #reset()}.
 *
 * <p>To change the behavior of one object, give it its own copy:
 *
 * <pre>{@code
 * Config cfg = Config.getGlobal().clone();
 * cfg.setMultiSection(true);
 *
 * Ini ini = new Ini();
 * ini.setConfig(cfg);
 * ini.load(new File("dwarfs.ini"));
 * }</pre>
 *
 * <p>A {@code Config} can be serialized, but the file encoding is not part of the serialized form:
 * it is {@code null} in a deserialized object.
 *
 * <p><strong>Thread safety.</strong> A {@code Config} has no synchronization. {@link #getGlobal()}
 * is shared by all the {@link Ini} objects that use it, so a change to it is a change to all of
 * them, and it is not safe while another thread reads or writes. Set up a configuration before
 * sharing it.
 *
 * @see Configurable
 * @since 0.4.0
 */
@SuppressWarnings("PMD.ExcessivePublicCount")
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({
  "SING_SINGLETON_IMPLEMENTS_CLONEABLE",
  "SING_SINGLETON_IMPLEMENTS_SERIALIZABLE"
})
public class Config implements Cloneable, Serializable {
  /**
   * The prefix of the system properties that set the initial values of a {@link Config}.
   *
   * <p>The full name of a property is this prefix followed by the {@code PROP_} constant of the
   * option, for example {@code org.ini4j.config.multiSection}.
   */
  public static final String KEY_PREFIX = "org.ini4j.config.";

  /**
   * The system property {@code org.ini4j.config.emptyOption} sets the initial value of {@link
   * #isEmptyOption()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_EMPTY_OPTION = "emptyOption";

  /**
   * The system property {@code org.ini4j.config.emptySection} sets the initial value of {@link
   * #isEmptySection()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_EMPTY_SECTION = "emptySection";

  /**
   * The system property {@code org.ini4j.config.globalSection} sets the initial value of {@link
   * #isGlobalSection()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_GLOBAL_SECTION = "globalSection";

  /**
   * The system property {@code org.ini4j.config.globalSectionName} sets the initial value of {@link
   * #getGlobalSectionName()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_GLOBAL_SECTION_NAME = "globalSectionName";

  /**
   * The system property {@code org.ini4j.config.include} sets the initial value of {@link
   * #isInclude()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_INCLUDE = "include";

  /**
   * The system property {@code org.ini4j.config.lowerCaseOption} sets the initial value of {@link
   * #isLowerCaseOption()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_LOWER_CASE_OPTION = "lowerCaseOption";

  /**
   * The system property {@code org.ini4j.config.lowerCaseSection} sets the initial value of {@link
   * #isLowerCaseSection()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_LOWER_CASE_SECTION = "lowerCaseSection";

  /**
   * The system property {@code org.ini4j.config.multiOption} sets the initial value of {@link
   * #isMultiOption()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_MULTI_OPTION = "multiOption";

  /**
   * The system property {@code org.ini4j.config.multiSection} sets the initial value of {@link
   * #isMultiSection()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_MULTI_SECTION = "multiSection";

  /**
   * The system property {@code org.ini4j.config.strictOperator} sets the initial value of {@link
   * #isStrictOperator()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_STRICT_OPERATOR = "strictOperator";

  /**
   * The system property {@code org.ini4j.config.unnamedSection} sets the initial value of {@link
   * #isUnnamedSection()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_UNNAMED_SECTION = "unnamedSection";

  /**
   * The system property {@code org.ini4j.config.escape} sets the initial value of {@link
   * #isEscape()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_ESCAPE = "escape";

  /**
   * The system property {@code org.ini4j.config.escapeNewline} sets the initial value of {@link
   * #isEscapeNewline()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_ESCAPE_NEWLINE = "escapeNewline";

  /**
   * The system property {@code org.ini4j.config.escapeKey} sets the initial value of {@link
   * #isEscapeKeyOnly()}. This constant is the part after {@link #KEY_PREFIX}.
   *
   * <p>The value is {@code escapeKey}, not {@code escapeKeyOnly}.
   */
  public static final String PROP_ESCAPE_KEY_ONLY = "escapeKey";

  /**
   * The system property {@code org.ini4j.config.pathSeparator} sets the initial value of {@link
   * #getPathSeparator()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_PATH_SEPARATOR = "pathSeparator";

  /**
   * The system property {@code org.ini4j.config.tree} sets the initial value of {@link #isTree()}.
   * This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_TREE = "tree";

  /**
   * The system property {@code org.ini4j.config.propertyFirstUpper} sets the initial value of
   * {@link #isPropertyFirstUpper()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_PROPERTY_FIRST_UPPER = "propertyFirstUpper";

  /**
   * The system property {@code org.ini4j.config.fileEncoding} sets the initial value of {@link
   * #getFileEncoding()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_FILE_ENCODING = "fileEncoding";

  /**
   * The system property {@code org.ini4j.config.lineSeparator} sets the initial value of {@link
   * #getLineSeparator()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_LINE_SEPARATOR = "lineSeparator";

  /**
   * The system property {@code org.ini4j.config.comment} sets the initial value of {@link
   * #isComment()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_COMMENT = "comment";

  /**
   * The system property {@code org.ini4j.config.headerComment} sets the initial value of {@link
   * #isHeaderComment()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_HEADER_COMMENT = "headerComment";

  /**
   * The system property {@code org.ini4j.config.maxResolveDepth} sets the initial value of {@link
   * #getMaxResolveDepth()}. This constant is the part after {@link #KEY_PREFIX}.
   */
  public static final String PROP_MAX_RESOLVE_DEPTH = "maxResolveDepth";

  /** The default of {@link #isEmptyOption()}: {@code false}. */
  public static final boolean DEFAULT_EMPTY_OPTION = false;

  /** The default of {@link #isEmptySection()}: {@code false}. */
  public static final boolean DEFAULT_EMPTY_SECTION = false;

  /** The default of {@link #isGlobalSection()}: {@code false}. */
  public static final boolean DEFAULT_GLOBAL_SECTION = false;

  /** The default of {@link #getGlobalSectionName()}: {@code "?"}. */
  public static final String DEFAULT_GLOBAL_SECTION_NAME = "?";

  /** The default of {@link #isInclude()}: {@code false}. */
  public static final boolean DEFAULT_INCLUDE = false;

  /** The default of {@link #isLowerCaseOption()}: {@code false}. */
  public static final boolean DEFAULT_LOWER_CASE_OPTION = false;

  /** The default of {@link #isLowerCaseSection()}: {@code false}. */
  public static final boolean DEFAULT_LOWER_CASE_SECTION = false;

  /** The default of {@link #isMultiOption()}: {@code true}. */
  public static final boolean DEFAULT_MULTI_OPTION = true;

  /** The default of {@link #isMultiSection()}: {@code false}. */
  public static final boolean DEFAULT_MULTI_SECTION = false;

  /** The default of {@link #isStrictOperator()}: {@code false}. */
  public static final boolean DEFAULT_STRICT_OPERATOR = false;

  /** The default of {@link #isUnnamedSection()}: {@code false}. */
  public static final boolean DEFAULT_UNNAMED_SECTION = false;

  /** The default of {@link #isEscape()}: {@code true}. */
  public static final boolean DEFAULT_ESCAPE = true;

  /** The default of {@link #isEscapeNewline()}: {@code true}. */
  public static final boolean DEFAULT_ESCAPE_NEWLINE = true;

  /** The default of {@link #isEscapeKeyOnly()}: {@code false}. */
  public static final boolean DEFAULT_ESCAPE_KEY_ONLY = false;

  /** The default of {@link #isTree()}: {@code true}. */
  public static final boolean DEFAULT_TREE = true;

  /** The default of {@link #isPropertyFirstUpper()}: {@code false}. */
  public static final boolean DEFAULT_PROPERTY_FIRST_UPPER = false;

  /** The default of {@link #isComment()}: {@code true}. */
  public static final boolean DEFAULT_COMMENT = true;

  /** The default of {@link #isHeaderComment()}: {@code true}. */
  public static final boolean DEFAULT_HEADER_COMMENT = true;

  /** The default of {@link #getPathSeparator()}: {@code '/'}. */
  public static final char DEFAULT_PATH_SEPARATOR = '/';

  /**
   * The default of {@link #getLineSeparator()}: the value of the system property {@code
   * line.separator}, or {@code "\n"} if that cannot be read.
   */
  public static final String DEFAULT_LINE_SEPARATOR = getSystemProperty("line.separator", "\n");

  /** The default of {@link #getFileEncoding()}: UTF-8. */
  public static final Charset DEFAULT_FILE_ENCODING = StandardCharsets.UTF_8;

  /** The default of {@link #getMaxResolveDepth()}: {@code 16}. */
  public static final int DEFAULT_MAX_RESOLVE_DEPTH = 16;

  private static final Config GLOBAL = new Config();
  private static final long serialVersionUID = 2865793267410367814L;
  private boolean _comment;
  private boolean _emptyOption;
  private boolean _emptySection;
  private boolean _escape;
  private boolean _escapeKeyOnly;
  private boolean _escapeNewline;
  private transient Charset _fileEncoding;
  private boolean _globalSection;
  private String _globalSectionName;
  private boolean _headerComment;
  private boolean _include;
  private String _lineSeparator;
  private boolean _lowerCaseOption;
  private boolean _lowerCaseSection;
  private boolean _multiOption;
  private boolean _multiSection;
  private char _pathSeparator;
  private boolean _propertyFirstUpper;
  private boolean _strictOperator;
  private boolean _tree;
  private boolean _unnamedSection;
  private int _maxResolveDepth;

  /**
   * Creates a configuration.
   *
   * <p>Every option gets the value of its system property, or its default if the property is not
   * set. See {@link #reset()}.
   *
   * @throws StringIndexOutOfBoundsException if the {@link #PROP_PATH_SEPARATOR} property is empty
   * @throws java.nio.charset.UnsupportedCharsetException if the {@link #PROP_FILE_ENCODING}
   *     property names an unknown character set
   * @throws java.nio.charset.IllegalCharsetNameException if the {@link #PROP_FILE_ENCODING}
   *     property is not a legal character set name
   */
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SING_SINGLETON_HAS_NONPRIVATE_CONSTRUCTOR")
  public Config() {
    reset();
  }

  /**
   * Returns an environment variable.
   *
   * @param name the name of the variable
   * @return the value, or {@code null} if the variable is not set or cannot be read
   */
  public static String getEnvironment(String name) {
    return getEnvironment(name, null);
  }

  /**
   * Returns an environment variable, or a default.
   *
   * <p>A {@link SecurityException} from the security manager is treated as if the variable were not
   * set.
   *
   * @param name the name of the variable
   * @param defaultValue the value to return when the variable is not set or cannot be read
   * @return the value of the variable, or {@code defaultValue}
   */
  public static String getEnvironment(String name, String defaultValue) {
    String value;

    try {
      value = System.getenv(name);
    } catch (SecurityException x) {
      value = null;
    }

    return (value == null) ? defaultValue : value;
  }

  /**
   * Returns the global configuration.
   *
   * <p>The global configuration is one shared object. {@link Ini} starts with it directly, while
   * {@link Options} and {@link Reg} start with a copy of it. {@link BasicProfile} and {@link
   * BasicOptionMap} objects that are not one of those classes use it too, and so do the parsers and
   * formatters of the {@code org.ini4j.spi} package until they are given another configuration. A
   * change to it therefore affects all of them. It reads the system properties once, when this
   * class is loaded.
   *
   * @return the global configuration, not a copy
   */
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("MS_EXPOSE_REP")
  public static Config getGlobal() {
    return GLOBAL;
  }

  /**
   * Returns a system property.
   *
   * @param name the name of the property
   * @return the value, or {@code null} if the property is not set or cannot be read
   */
  public static String getSystemProperty(String name) {
    return getSystemProperty(name, null);
  }

  /**
   * Returns a system property, or a default.
   *
   * <p>A {@link SecurityException} from the security manager is treated as if the property were not
   * set.
   *
   * @param name the name of the property
   * @param defaultValue the value to return when the property is not set or cannot be read
   * @return the value of the property, or {@code defaultValue}
   */
  public static String getSystemProperty(String name, String defaultValue) {
    String value;

    try {
      value = System.getProperty(name);
    } catch (SecurityException x) {
      value = null;
    }

    return (value == null) ? defaultValue : value;
  }

  /**
   * Sets the value of {@link #isComment()}.
   *
   * @param value the new value
   */
  public void setComment(boolean value) {
    _comment = value;
  }

  /**
   * Tells whether special characters are escaped with a backslash.
   *
   * <p>When it is on, the parser decodes backslash sequences in names and values: {@code \t},
   * {@code \n}, {@code \f}, {@code \b}, {@code \r}, {@code \:}, {@code \=} and {@code \\}, and a
   * backslash followed by {@code u} and four hexadecimal digits for a character by its code. A
   * backslash before any other character is dropped. A malformed sequence with {@code u} throws an
   * {@link IllegalArgumentException}. The formatter writes those characters in the same form, and
   * every other character outside the printable ASCII range with the {@code u} form, even if the
   * file encoding could hold it. Section names are escaped like option names. See also {@link
   * #isEscapeKeyOnly()}.
   *
   * <p>{@link Reg} starts with it off.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.escape} if it is
   * set, otherwise from {@link #DEFAULT_ESCAPE}.
   *
   * @return {@code true} if it is on
   */
  public boolean isEscape() {
    return _escape;
  }

  /**
   * Tells whether a backslash at the end of a line continues the line.
   *
   * <p>When it is on, the parser removes a backslash that ends a line (an odd number of
   * backslashes) and appends the next line, with its leading white space removed, to the same
   * logical line. This applies to the parser only.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.escapeNewline} if
   * it is set, otherwise from {@link #DEFAULT_ESCAPE_NEWLINE}.
   *
   * @return {@code true} if it is on
   */
  public boolean isEscapeNewline() {
    return _escapeNewline;
  }

  /**
   * Tells whether the parser follows include lines.
   *
   * <p>When it is on, a line of the form {@code <location>} is replaced by the content of that
   * location, which is read as part of the same input. A line of the form {@code <?location>} is
   * the same, but an {@link java.io.IOException} when opening the location is ignored. A relative
   * location is resolved against the URL that is being loaded. When the input is a stream or a
   * reader, the location must be an absolute URL. {@link Reg} reads through a reader even when it
   * loads a file or a URL, so it needs absolute locations. The line is recognized only when its
   * first character is {@code <} and its last is {@code >}.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.include} if it is
   * set, otherwise from {@link #DEFAULT_INCLUDE}.
   *
   * @return {@code true} if it is on
   */
  public boolean isInclude() {
    return _include;
  }

  /**
   * Tells whether adding a section also adds its missing parents.
   *
   * <p>When it is on, {@link Profile#add(String)} on an {@link Ini} or a {@link Reg} adds the
   * sections for the missing levels of the name first, so that adding {@code a/b/c} also adds
   * {@code a} and {@code a/b}. The tree methods of {@link Profile.Section} work either way, but
   * they find only the sections that exist.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.tree} if it is set,
   * otherwise from {@link #DEFAULT_TREE}.
   *
   * @return {@code true} if it is on
   */
  public boolean isTree() {
    return _tree;
  }

  /**
   * Sets the value of {@link #isEmptyOption()}.
   *
   * @param value the new value
   */
  public void setEmptyOption(boolean value) {
    _emptyOption = value;
  }

  /**
   * Sets the value of {@link #isEmptySection()}.
   *
   * @param value the new value
   */
  public void setEmptySection(boolean value) {
    _emptySection = value;
  }

  /**
   * Sets the value of {@link #isEscape()}.
   *
   * @param value the new value
   */
  public void setEscape(boolean value) {
    _escape = value;
  }

  /**
   * Sets the value of {@link #isEscapeKeyOnly()}.
   *
   * @param value the new value
   */
  public void setEscapeKeyOnly(boolean value) {
    _escapeKeyOnly = value;
  }

  /**
   * Sets the value of {@link #isEscapeNewline()}.
   *
   * @param value the new value
   */
  public void setEscapeNewline(boolean value) {
    _escapeNewline = value;
  }

  /**
   * Returns the character set used to read and write streams and files.
   *
   * <p>It is used by the load and store methods that take a stream, a file or a URL. The methods
   * that take a {@link java.io.Reader} or a {@link java.io.Writer} do not use it. When an {@link
   * Ini} or an {@link Options} loads from a URL or a file, a byte order mark at the start of the
   * input overrides it. {@link Reg} starts with UTF-16 little endian.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.fileEncoding} if it
   * is set, otherwise from {@link #DEFAULT_FILE_ENCODING}.
   *
   * @return the character set
   */
  public Charset getFileEncoding() {
    return _fileEncoding;
  }

  /**
   * Sets the value of {@link #getFileEncoding()}.
   *
   * @param value the new value
   */
  public void setFileEncoding(Charset value) {
    _fileEncoding = value;
  }

  /**
   * Sets the value of {@link #isGlobalSection()}.
   *
   * @param value the new value
   */
  public void setGlobalSection(boolean value) {
    _globalSection = value;
  }

  /**
   * Returns the name of the section that holds the options before the first section header.
   *
   * <p>It is used only when {@link #isGlobalSection()} is on.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.globalSectionName}
   * if it is set, otherwise from {@link #DEFAULT_GLOBAL_SECTION_NAME}.
   *
   * @return the section name
   */
  public String getGlobalSectionName() {
    return _globalSectionName;
  }

  /**
   * Sets the value of {@link #getGlobalSectionName()}.
   *
   * @param value the new value
   */
  public void setGlobalSectionName(String value) {
    _globalSectionName = value;
  }

  /**
   * Sets the value of {@link #isHeaderComment()}.
   *
   * @param value the new value
   */
  public void setHeaderComment(boolean value) {
    _headerComment = value;
  }

  /**
   * Sets the value of {@link #isInclude()}.
   *
   * @param value the new value
   */
  public void setInclude(boolean value) {
    _include = value;
  }

  /**
   * Returns the line separator.
   *
   * <p>The formatter ends every line with it. The parser joins the lines of a multi-line comment
   * with it, and the formatter splits a comment into lines at it. {@link Reg} starts with {@code CR
   * LF}.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.lineSeparator} if
   * it is set, otherwise from {@link #DEFAULT_LINE_SEPARATOR}.
   *
   * @return the line separator
   */
  public String getLineSeparator() {
    return _lineSeparator;
  }

  /**
   * Sets the value of {@link #getLineSeparator()}.
   *
   * @param value the new value
   */
  public void setLineSeparator(String value) {
    _lineSeparator = value;
  }

  /**
   * Sets the value of {@link #isLowerCaseOption()}.
   *
   * @param value the new value
   */
  public void setLowerCaseOption(boolean value) {
    _lowerCaseOption = value;
  }

  /**
   * Sets the value of {@link #isLowerCaseSection()}.
   *
   * @param value the new value
   */
  public void setLowerCaseSection(boolean value) {
    _lowerCaseSection = value;
  }

  /**
   * Sets the value of {@link #isMultiOption()}.
   *
   * @param value the new value
   */
  public void setMultiOption(boolean value) {
    _multiOption = value;
  }

  /**
   * Sets the value of {@link #isMultiSection()}.
   *
   * @param value the new value
   */
  public void setMultiSection(boolean value) {
    _multiSection = value;
  }

  /**
   * Tells whether an option may have no value.
   *
   * <p>When it is on, the parser accepts a line that has no operator and reads it as an option with
   * that text as its name and a {@code null} value. The formatter writes a {@code null} value as an
   * empty value. When it is off, the parser reports such a line as an {@link
   * InvalidFileFormatException} and the formatter leaves the option out.
   *
   * <p>{@link Options} and {@link Reg} start with it on.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.emptyOption} if it
   * is set, otherwise from {@link #DEFAULT_EMPTY_OPTION}.
   *
   * @return {@code true} if it is on
   */
  public boolean isEmptyOption() {
    return _emptyOption;
  }

  /**
   * Tells whether sections without options are stored.
   *
   * <p>When it is off, {@link Ini#store(java.io.Writer)} leaves out the sections that have no
   * options. Loading is not affected: a section with no options in the input is loaded either way.
   *
   * <p>{@link Reg} starts with it on.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.emptySection} if it
   * is set, otherwise from {@link #DEFAULT_EMPTY_SECTION}.
   *
   * @return {@code true} if it is on
   */
  public boolean isEmptySection() {
    return _emptySection;
  }

  /**
   * Tells whether options before the first section header are allowed.
   *
   * <p>When it is on, the parser puts such options in a section named {@link
   * #getGlobalSectionName()}. The formatter writes the options of that section without a section
   * header. When it is off, the parser reports an option before the first header as an {@link
   * InvalidFileFormatException}.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.globalSection} if
   * it is set, otherwise from {@link #DEFAULT_GLOBAL_SECTION}.
   *
   * @return {@code true} if it is on
   */
  public boolean isGlobalSection() {
    return _globalSection;
  }

  /**
   * Tells whether the parser converts option names to lower case.
   *
   * <p>The conversion uses the default locale. It applies when loading only; names set in code are
   * not changed.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.lowerCaseOption} if
   * it is set, otherwise from {@link #DEFAULT_LOWER_CASE_OPTION}.
   *
   * @return {@code true} if it is on
   */
  public boolean isLowerCaseOption() {
    return _lowerCaseOption;
  }

  /**
   * Tells whether the parser converts section names to lower case.
   *
   * <p>The conversion uses the default locale. It applies when loading only; names set in code are
   * not changed.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.lowerCaseSection}
   * if it is set, otherwise from {@link #DEFAULT_LOWER_CASE_SECTION}.
   *
   * @return {@code true} if it is on
   */
  public boolean isLowerCaseSection() {
    return _lowerCaseSection;
  }

  /**
   * Tells whether an option name can have several values.
   *
   * <p>When it is on, the parser keeps every value of an option that appears more than once in a
   * section, and the formatter writes them all. When it is off, a later value replaces the earlier
   * one when loading. When storing an {@link Ini}, only the last value of each option is written,
   * and when storing an {@link Options}, only the first. {@link Reg} starts with it on, and always
   * writes all the values of a registry value name.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.multiOption} if it
   * is set, otherwise from {@link #DEFAULT_MULTI_OPTION}.
   *
   * @return {@code true} if it is on
   */
  public boolean isMultiOption() {
    return _multiOption;
  }

  /**
   * Tells whether a section name can be used for several sections.
   *
   * <p>When it is on, every section header in the input adds a new section, even if a section of
   * that name exists already. When it is off, the options under a repeated header are added to the
   * existing section. It applies when loading only.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.multiSection} if it
   * is set, otherwise from {@link #DEFAULT_MULTI_SECTION}.
   *
   * @return {@code true} if it is on
   */
  public boolean isMultiSection() {
    return _multiSection;
  }

  /**
   * Tells whether a section may have an empty name.
   *
   * <p>When it is on, the parser accepts the header {@code []}. When it is off, it reports that
   * header as an {@link InvalidFileFormatException}.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.unnamedSection} if
   * it is set, otherwise from {@link #DEFAULT_UNNAMED_SECTION}.
   *
   * @return {@code true} if it is on
   */
  public boolean isUnnamedSection() {
    return _unnamedSection;
  }

  /**
   * Returns the character that separates the levels of a section name.
   *
   * <p>A section named {@code a/b} (with the default) is a child of the section {@code a}. See
   * {@link #isTree()} and {@link Profile.Section}. {@link Reg} starts with a backslash.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.pathSeparator} if
   * it is set, otherwise from {@link #DEFAULT_PATH_SEPARATOR}.
   *
   * @return the path separator
   */
  public char getPathSeparator() {
    return _pathSeparator;
  }

  /**
   * Sets the value of {@link #getPathSeparator()}.
   *
   * @param value the new value
   */
  public void setPathSeparator(char value) {
    _pathSeparator = value;
  }

  /**
   * Sets the value of {@link #isPropertyFirstUpper()}.
   *
   * @param value the new value
   */
  public void setPropertyFirstUpper(boolean value) {
    _propertyFirstUpper = value;
  }

  /**
   * Tells whether bean property names start with an upper case letter in the file.
   *
   * <p>When it is on, the bean methods ({@link OptionMap#as(Class)}, {@link
   * OptionMap#from(Object)}, {@link OptionMap#to(Object)} and {@link Profile#as(Class)}) change the
   * first letter of the property name to upper case to get the option or section name, so the
   * property {@code age} is the option {@code Age}.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.propertyFirstUpper}
   * if it is set, otherwise from {@link #DEFAULT_PROPERTY_FIRST_UPPER}.
   *
   * @return {@code true} if it is on
   */
  public boolean isPropertyFirstUpper() {
    return _propertyFirstUpper;
  }

  /**
   * Tells whether options are written without spaces around the operator.
   *
   * <p>When it is on, an option is written as {@code name=value}. When it is off, it is written as
   * {@code name = value}. The parser accepts both forms either way.
   *
   * <p>{@link Reg} starts with it on.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.strictOperator} if
   * it is set, otherwise from {@link #DEFAULT_STRICT_OPERATOR}.
   *
   * @return {@code true} if it is on
   */
  public boolean isStrictOperator() {
    return _strictOperator;
  }

  /**
   * Sets the value of {@link #isStrictOperator()}.
   *
   * @param value the new value
   */
  public void setStrictOperator(boolean value) {
    _strictOperator = value;
  }

  /**
   * Tells whether comments are loaded and stored.
   *
   * <p>When it is on, the parser keeps comments and the formatter writes them, as described in
   * {@link CommentedMap}. When it is off, comments are skipped when loading and not written when
   * storing.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.comment} if it is
   * set, otherwise from {@link #DEFAULT_COMMENT}.
   *
   * @return {@code true} if it is on
   */
  public boolean isComment() {
    return _comment;
  }

  /**
   * Tells whether the first comment of a file is its header comment.
   *
   * <p>When it is on, the comment at the top of the input, before the first section or option, is
   * loaded as the comment of the whole {@link Profile} or {@link Options} object, and the formatter
   * writes that comment first, followed by an empty line. When it is off, that comment belongs to
   * the first section or option, and the formatter does not write the comment of the whole object.
   * It works together with {@link #isComment()}.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.headerComment} if
   * it is set, otherwise from {@link #DEFAULT_HEADER_COMMENT}.
   *
   * @return {@code true} if it is on
   */
  public boolean isHeaderComment() {
    return _headerComment;
  }

  /**
   * Sets the value of {@link #isTree()}.
   *
   * @param value the new value
   */
  public void setTree(boolean value) {
    _tree = value;
  }

  /**
   * Sets the value of {@link #isUnnamedSection()}.
   *
   * @param value the new value
   */
  public void setUnnamedSection(boolean value) {
    _unnamedSection = value;
  }

  /**
   * Tells whether only names are escaped, not values.
   *
   * <p>It has an effect only when {@link #isEscape()} is on. Then names and section names are
   * escaped and unescaped as usual, and values are not escaped or unescaped.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.escapeKey} if it is
   * set, otherwise from {@link #DEFAULT_ESCAPE_KEY_ONLY}.
   *
   * @return {@code true} if it is on
   */
  public boolean isEscapeKeyOnly() {
    return _escapeKeyOnly;
  }

  /**
   * Returns the longest chain of variable references that is resolved.
   *
   * <p>A value can contain variables whose values contain variables again. With a limit of {@code
   * n}, a chain of {@code n} references is resolved, and a longer chain throws a {@link
   * CircularReferenceException}. A value is only checked when it contains a dollar sign, and with a
   * limit of {@code 0} every such value throws. The limit stops circular references, which would
   * otherwise never end. A system property that is not a number is ignored. See {@link OptionMap}.
   *
   * <p>The initial value comes from the system property {@code org.ini4j.config.maxResolveDepth} if
   * it is set, otherwise from {@link #DEFAULT_MAX_RESOLVE_DEPTH}.
   *
   * @return the limit
   */
  public int getMaxResolveDepth() {
    return _maxResolveDepth;
  }

  /**
   * Sets the value of {@link #getMaxResolveDepth()}.
   *
   * @param value the new value
   */
  public void setMaxResolveDepth(int value) {
    _maxResolveDepth = value;
  }

  /**
   * Returns a copy of this configuration.
   *
   * @return a new configuration with the same values
   */
  @Override
  public Config clone() {
    try {
      return (Config) super.clone();
    } catch (CloneNotSupportedException x) {
      throw new AssertionError(x);
    }
  }

  /**
   * Sets every option to the value of its system property, or to its default if the property is not
   * set.
   *
   * <p>Values set with the setters are lost. A boolean property with a value other than {@code
   * true} (in any letter case) is read as {@code false}, and a number property that cannot be
   * parsed is ignored.
   *
   * @throws StringIndexOutOfBoundsException if the {@link #PROP_PATH_SEPARATOR} property is empty
   * @throws java.nio.charset.UnsupportedCharsetException if the {@link #PROP_FILE_ENCODING}
   *     property names an unknown character set
   * @throws java.nio.charset.IllegalCharsetNameException if the {@link #PROP_FILE_ENCODING}
   *     property is not a legal character set name
   */
  public final void reset() {
    _emptyOption = getBoolean(PROP_EMPTY_OPTION, DEFAULT_EMPTY_OPTION);
    _emptySection = getBoolean(PROP_EMPTY_SECTION, DEFAULT_EMPTY_SECTION);
    _globalSection = getBoolean(PROP_GLOBAL_SECTION, DEFAULT_GLOBAL_SECTION);
    _globalSectionName = getString(PROP_GLOBAL_SECTION_NAME, DEFAULT_GLOBAL_SECTION_NAME);
    _include = getBoolean(PROP_INCLUDE, DEFAULT_INCLUDE);
    _lowerCaseOption = getBoolean(PROP_LOWER_CASE_OPTION, DEFAULT_LOWER_CASE_OPTION);
    _lowerCaseSection = getBoolean(PROP_LOWER_CASE_SECTION, DEFAULT_LOWER_CASE_SECTION);
    _multiOption = getBoolean(PROP_MULTI_OPTION, DEFAULT_MULTI_OPTION);
    _multiSection = getBoolean(PROP_MULTI_SECTION, DEFAULT_MULTI_SECTION);
    _strictOperator = getBoolean(PROP_STRICT_OPERATOR, DEFAULT_STRICT_OPERATOR);
    _unnamedSection = getBoolean(PROP_UNNAMED_SECTION, DEFAULT_UNNAMED_SECTION);
    _escape = getBoolean(PROP_ESCAPE, DEFAULT_ESCAPE);
    _escapeKeyOnly = getBoolean(PROP_ESCAPE_KEY_ONLY, DEFAULT_ESCAPE_KEY_ONLY);
    _escapeNewline = getBoolean(PROP_ESCAPE_NEWLINE, DEFAULT_ESCAPE_NEWLINE);
    _pathSeparator = getChar(PROP_PATH_SEPARATOR, DEFAULT_PATH_SEPARATOR);
    _tree = getBoolean(PROP_TREE, DEFAULT_TREE);
    _propertyFirstUpper = getBoolean(PROP_PROPERTY_FIRST_UPPER, DEFAULT_PROPERTY_FIRST_UPPER);
    _lineSeparator = getString(PROP_LINE_SEPARATOR, DEFAULT_LINE_SEPARATOR);
    _fileEncoding = getCharset(PROP_FILE_ENCODING, DEFAULT_FILE_ENCODING);
    _comment = getBoolean(PROP_COMMENT, DEFAULT_COMMENT);
    _headerComment = getBoolean(PROP_HEADER_COMMENT, DEFAULT_HEADER_COMMENT);
    _maxResolveDepth = getInt(PROP_MAX_RESOLVE_DEPTH, DEFAULT_MAX_RESOLVE_DEPTH);
  }

  private boolean getBoolean(String name, boolean defaultValue) {
    String value = getSystemProperty(KEY_PREFIX + name);

    return (value == null) ? defaultValue : Boolean.parseBoolean(value);
  }

  private char getChar(String name, char defaultValue) {
    String value = getSystemProperty(KEY_PREFIX + name);

    return (value == null) ? defaultValue : value.charAt(0);
  }

  private Charset getCharset(String name, Charset defaultValue) {
    String value = getSystemProperty(KEY_PREFIX + name);

    return (value == null) ? defaultValue : Charset.forName(value);
  }

  private String getString(String name, String defaultValue) {
    return getSystemProperty(KEY_PREFIX + name, defaultValue);
  }

  private int getInt(String name, int defaultValue) {
    String value = getSystemProperty(KEY_PREFIX + name);

    if (value == null) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
