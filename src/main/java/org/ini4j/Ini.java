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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import org.ini4j.spi.IniBuilder;
import org.ini4j.spi.IniFormatter;
import org.ini4j.spi.IniHandler;
import org.ini4j.spi.IniParser;

/**
 * An INI file in memory: a {@link Profile} that can be loaded from and stored to a file.
 *
 * <p>An {@code Ini} holds named sections, each with options. A section name can be used more than
 * once and an option can have several values (see {@link MultiMap} and the flags {@link
 * Config#isMultiSection()} and {@link Config#isMultiOption()}). Values can contain variables,
 * comments are kept, and the sections can be read as a tree or through your own interfaces (see
 * {@link Profile}).
 *
 * <p>A new {@code Ini} uses the global {@link Config} itself, not a copy (see {@link
 * Config#getGlobal()}). Call {@link #setConfig(Config)} to give it its own. Loading and storing go
 * through {@link org.ini4j.spi.IniParser} and {@link org.ini4j.spi.IniFormatter}, with the
 * configuration of this object. {@code load} adds to the existing content, as described in {@link
 * Persistable}.
 *
 * <p>The methods that read or write a stream use the file encoding of the configuration. When a
 * file or a URL is loaded, a byte order mark at the start of the input takes precedence over it.
 * When an {@link java.io.InputStream} is loaded, there is no such detection.
 *
 * <p>This reads two options and writes a new section:
 *
 * <pre>{@code
 * Ini ini = new Ini(new File("dwarfs.ini"));
 * int age = ini.get("happy", "age", int.class);
 * String dir = ini.get("happy", "homeDir");
 *
 * ini.put("sleepy", "age", 55);
 * ini.store();
 * }</pre>
 *
 * <p>The thread safety of {@link BasicMultiMap} applies.
 *
 * @see Wini
 * @see Options
 * @since 0.3.0
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"CT_CONSTRUCTOR_THROW", "EI_EXPOSE_REP"})
public class Ini extends BasicProfile implements Persistable, Configurable {
  private static final long serialVersionUID = -6029486578113700585L;
  private Config _config;
  private File _file;

  /** Creates an empty {@code Ini} that uses the global {@link Config}. */
  public Ini() {
    _config = Config.getGlobal();
  }

  /**
   * Creates an {@code Ini} and loads it from a reader.
   *
   * @param input the reader to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Ini(Reader input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Creates an {@code Ini} and loads it from a stream.
   *
   * @param input the stream to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Ini(InputStream input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Creates an {@code Ini} and loads it from a URL.
   *
   * @param input the location to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Ini(URL input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Creates an {@code Ini}, remembers the file and loads it.
   *
   * <p>The file stays the associated file, so {@link #store()} writes back to it.
   *
   * @param input the file to read and later to write
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Ini(File input) throws IOException, InvalidFileFormatException {
    this();
    _file = input;
    load();
  }

  @Override
  public Config getConfig() {
    return _config;
  }

  @Override
  public void setConfig(Config value) {
    _config = value;
  }

  @Override
  public File getFile() {
    return _file;
  }

  @Override
  public void setFile(File value) {
    _file = value;
  }

  @Override
  public void load() throws IOException, InvalidFileFormatException {
    if (_file == null) {
      throw new FileNotFoundException();
    }

    load(_file);
  }

  @Override
  public void load(InputStream input) throws IOException, InvalidFileFormatException {
    load(new InputStreamReader(input, getConfig().getFileEncoding()));
  }

  @Override
  public void load(Reader input) throws IOException, InvalidFileFormatException {
    IniParser.newInstance(getConfig()).parse(input, newBuilder());
  }

  @Override
  public void load(File input) throws IOException, InvalidFileFormatException {
    load(input.toURI().toURL());
  }

  @Override
  public void load(URL input) throws IOException, InvalidFileFormatException {
    IniParser.newInstance(getConfig()).parse(input, newBuilder());
  }

  @Override
  public void store() throws IOException {
    if (_file == null) {
      throw new FileNotFoundException();
    }

    store(_file);
  }

  @Override
  public void store(OutputStream output) throws IOException {
    store(new OutputStreamWriter(output, getConfig().getFileEncoding()));
  }

  @Override
  public void store(Writer output) throws IOException {
    store(IniFormatter.newInstance(output, getConfig()));
  }

  @Override
  public void store(File output) throws IOException {
    OutputStream stream = new FileOutputStream(output);

    try {
      store(stream);
    } finally {
      stream.close();
    }
  }

  /**
   * Creates the handler that builds this object while a file is parsed.
   *
   * <p>A subclass can override it to change how the parsed events are turned into sections and
   * options.
   *
   * @return a new builder for this object
   */
  protected IniHandler newBuilder() {
    return IniBuilder.newInstance(this);
  }

  /**
   * Sends a section to a formatter, unless it is left out.
   *
   * <p>A section without options is left out when {@link Config#isEmptySection()} is off. A
   * subclass can override this method to change what is stored.
   *
   * @param formatter the handler that receives the events
   * @param section the section to send
   */
  @Override
  protected void store(IniHandler formatter, Profile.Section section) {
    if (getConfig().isEmptySection() || (section.size() != 0)) {
      super.store(formatter, section);
    }
  }

  /**
   * Sends one value of an option to a formatter, unless it is left out.
   *
   * <p>When {@link Config#isMultiOption()} is off, only the last value of an option is sent. A
   * subclass can override this method to change what is stored.
   *
   * @param formatter the handler that receives the events
   * @param section the section of the option
   * @param option the option name
   * @param index the position of the value
   */
  @Override
  protected void store(IniHandler formatter, Profile.Section section, String option, int index) {
    if (getConfig().isMultiOption() || (index == (section.length(option) - 1))) {
      super.store(formatter, section, option, index);
    }
  }

  @Override
  boolean isTreeMode() {
    return getConfig().isTree();
  }

  @Override
  char getPathSeparator() {
    return getConfig().getPathSeparator();
  }

  @Override
  boolean isPropertyFirstUpper() {
    return getConfig().isPropertyFirstUpper();
  }
}
