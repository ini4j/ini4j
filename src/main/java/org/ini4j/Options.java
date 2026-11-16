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
import org.ini4j.spi.OptionsBuilder;
import org.ini4j.spi.OptionsFormatter;
import org.ini4j.spi.OptionsHandler;
import org.ini4j.spi.OptionsParser;

/**
 * An {@link OptionMap} that can be loaded from and stored to a file of options without sections.
 *
 * <p>The file has one option on each line, written as {@code name=value} or {@code name:value}. A
 * line that starts with {@code !} or {@code #} is a comment. It looks like a Java properties file.
 * Unlike a properties file, an option name can have several values, comments are kept, values can
 * contain variables (see {@link OptionMap}), and the content can be read through your own
 * interfaces.
 *
 * <p>A new {@code Options} has its own copy of the global {@link Config}, with {@link
 * Config#isEmptyOption()} turned on. The methods that read or write a stream use its file encoding.
 * When a file or a URL is loaded, a byte order mark at the start of the input takes precedence over
 * it.
 *
 * <p>This reads an option and writes the file back:
 *
 * <pre>{@code
 * Options opt = new Options(new File("dwarfs.opt"));
 * String home = opt.get("homeDir");
 *
 * opt.put("age", 33);
 * opt.store();
 * }</pre>
 *
 * <p>The thread safety of {@link BasicMultiMap} applies.
 *
 * @see Ini
 * @since 0.4.1
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"CT_CONSTRUCTOR_THROW", "EI_EXPOSE_REP"})
public class Options extends BasicOptionMap implements Persistable, Configurable {
  private static final long serialVersionUID = -1119753444859181822L;
  private String _comment;
  private Config _config;
  private File _file;

  /** Creates an empty {@code Options} with its own configuration. */
  public Options() {
    _config = Config.getGlobal().clone();
    _config.setEmptyOption(true);
  }

  /**
   * Creates an {@code Options} and loads it from a reader.
   *
   * @param input the reader to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Options(Reader input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Creates an {@code Options} and loads it from a stream.
   *
   * @param input the stream to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Options(InputStream input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Creates an {@code Options} and loads it from a URL.
   *
   * @param input the location to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Options(URL input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Creates an {@code Options}, remembers the file and loads it.
   *
   * <p>The file stays the associated file, so {@link #store()} writes back to it.
   *
   * @param input the file to read and later to write
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  public Options(File input) throws IOException, InvalidFileFormatException {
    this();
    _file = input;
    load();
  }

  /**
   * Returns the header comment of the options.
   *
   * <p>When a file is loaded with {@link Config#isHeaderComment()} and {@link Config#isComment()}
   * on, the comment at the top of the file, before the first option, becomes the header comment.
   *
   * @return the comment, or {@code null} if there is none
   */
  public String getComment() {
    return _comment;
  }

  /**
   * Sets the header comment of the options.
   *
   * @param value the new comment, or {@code null} to have none
   */
  public void setComment(String value) {
    _comment = value;
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
    OptionsParser.newInstance(getConfig()).parse(input, newBuilder());
  }

  @Override
  public void load(URL input) throws IOException, InvalidFileFormatException {
    OptionsParser.newInstance(getConfig()).parse(input, newBuilder());
  }

  @Override
  public void load(File input) throws IOException, InvalidFileFormatException {
    load(input.toURI().toURL());
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
    store(OptionsFormatter.newInstance(output, getConfig()));
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
   * <p>A subclass can override it to change how the parsed events are turned into options.
   *
   * @return a new builder for this object
   */
  protected OptionsHandler newBuilder() {
    return OptionsBuilder.newInstance(this);
  }

  /**
   * Sends the content to a formatter.
   *
   * <p>The handler gets {@link OptionsHandler#startOptions()}, the header comment, then for each
   * option its comment and its values, and {@link OptionsHandler#endOptions()}. When {@link
   * Config#isMultiOption()} is off, only the first value of each option is sent. A subclass can
   * override this method to change what is stored.
   *
   * @param formatter the handler that receives the events
   * @throws IOException if the handler fails
   */
  protected void store(OptionsHandler formatter) throws IOException {
    formatter.startOptions();
    storeComment(formatter, _comment);
    for (String name : keySet()) {
      storeComment(formatter, getComment(name));
      int n = getConfig().isMultiOption() ? length(name) : 1;

      for (int i = 0; i < n; i++) {
        String value = get(name, i);

        formatter.handleOption(name, value);
      }
    }

    formatter.endOptions();
  }

  @Override
  boolean isPropertyFirstUpper() {
    return getConfig().isPropertyFirstUpper();
  }

  private void storeComment(OptionsHandler formatter, String comment) {
    formatter.handleComment(comment);
  }
}
