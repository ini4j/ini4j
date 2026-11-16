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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import org.ini4j.spi.IniFormatter;
import org.ini4j.spi.IniHandler;
import org.ini4j.spi.IniParser;
import org.ini4j.spi.RegBuilder;

/**
 * A {@link Registry} that reads and writes Windows registry export files, and on Windows can
 * exchange keys with the real registry.
 *
 * <p>A {@code .reg} file starts with a version header (see {@link Registry#VERSION}), followed by
 * keys written as sections, whose options are the registry values with their types. The model is
 * described in {@link Registry}. The load methods require the first line to be the version header,
 * and they skip the line after it.
 *
 * <p>A new {@code Reg} has its own copy of the global {@link Config} with the settings of this
 * format: escaping is off, the global section is off, an option without a value, an option with
 * several values and a section without options are allowed, operators are written without spaces,
 * the path separator is a backslash ({@link Registry#KEY_SEPARATOR}), the file encoding is {@link
 * Registry#FILE_ENCODING} and the line separator is {@link Registry#LINE_SEPARATOR}.
 *
 * <p>Only the methods {@link #read(String)}, {@link #write()} and the constructor that takes a key
 * name work with the real registry. They run the Windows {@code reg} command, so they are for
 * Windows only (see {@link #isWindows()}) and throw an {@link UnsupportedOperationException}
 * elsewhere. All the other methods work on every system.
 *
 * <p>The thread safety of {@link BasicMultiMap} applies.
 *
 * @see Registry
 * @since 0.5.0
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"CT_CONSTRUCTOR_THROW", "EI_EXPOSE_REP"})
public class Reg extends BasicRegistry implements Persistable, Configurable {
  private static final long serialVersionUID = -1485602876922985912L;

  /**
   * The file name suffix of the temporary files used by {@link #read(String)} and {@link #write()}.
   */
  protected static final String DEFAULT_SUFFIX = ".reg";

  /**
   * The file name prefix of the temporary files used by {@link #read(String)} and {@link #write()}.
   */
  protected static final String TMP_PREFIX = "reg-";

  private static final int STDERR_BUFF_SIZE = 8192;
  private static final String PROP_OS_NAME = "os.name";
  private static final boolean WINDOWS =
      Config.getSystemProperty(PROP_OS_NAME, "Unknown").startsWith("Windows");
  private static final char CR = '\r';
  private static final char LF = '\n';
  private Config _config;
  private File _file;

  /**
   * Creates an empty registry with its own configuration, which has the settings of the registry
   * file format.
   */
  public Reg() {
    Config cfg = Config.getGlobal().clone();

    cfg.setEscape(false);
    cfg.setGlobalSection(false);
    cfg.setEmptyOption(true);
    cfg.setMultiOption(true);
    cfg.setStrictOperator(true);
    cfg.setEmptySection(true);
    cfg.setPathSeparator(KEY_SEPARATOR);
    cfg.setFileEncoding(FILE_ENCODING);
    cfg.setLineSeparator(LINE_SEPARATOR);
    _config = cfg;
  }

  /**
   * Creates a registry and reads a key of the real registry into it.
   *
   * <p>It is the same as {@link #Reg()} followed by {@link #read(String)}.
   *
   * @param registryKey the path of the key, for example {@code HKEY_CURRENT_USER\Software\ini4j}
   * @throws IOException if the {@code reg} command fails
   * @throws UnsupportedOperationException if this is not Windows
   */
  public Reg(String registryKey) throws IOException {
    this();
    read(registryKey);
  }

  /**
   * Creates a registry, remembers the file and loads it.
   *
   * @param input the file to read and later to write
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the version header is missing or unsupported, or the
   *     content is not valid
   */
  public Reg(File input) throws IOException, InvalidFileFormatException {
    this();
    _file = input;
    load();
  }

  /**
   * Creates a registry and loads it from a URL.
   *
   * @param input the location to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the version header is missing or unsupported, or the
   *     content is not valid
   */
  public Reg(URL input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Creates a registry and loads it from a stream.
   *
   * @param input the stream to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the version header is missing or unsupported, or the
   *     content is not valid
   */
  public Reg(InputStream input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Creates a registry and loads it from a reader.
   *
   * @param input the reader to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the version header is missing or unsupported, or the
   *     content is not valid
   */
  public Reg(Reader input) throws IOException, InvalidFileFormatException {
    this();
    load(input);
  }

  /**
   * Tells whether the program runs on Windows.
   *
   * <p>The answer is taken from the system property {@code os.name}, which must start with {@code
   * Windows}. It is read once, when this class is loaded.
   *
   * @return {@code true} on Windows
   */
  public static boolean isWindows() {
    return WINDOWS;
  }

  @Override
  public Config getConfig() {
    return _config;
  }

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
  public void load(URL input) throws IOException, InvalidFileFormatException {
    load(new InputStreamReader(input.openStream(), getConfig().getFileEncoding()));
  }

  @Override
  public void load(Reader input) throws IOException, InvalidFileFormatException {
    int newline = 2;
    StringBuilder buff = new StringBuilder();

    for (int c = input.read(); c != -1; c = input.read()) {
      if (c == LF) {
        newline--;
        if (newline == 0) {
          break;
        }
      } else if ((c != CR) && (newline != 1)) {
        buff.append((char) c);
      }
    }

    if (buff.length() == 0) {
      throw new InvalidFileFormatException("Missing version header");
    }

    if (!buff.toString().equals(getVersion())) {
      throw new InvalidFileFormatException("Unsupported version: " + buff.toString());
    }

    IniParser.newInstance(getConfig()).parse(input, newBuilder());
  }

  @Override
  public void load(File input) throws IOException, InvalidFileFormatException {
    load(input.toURI().toURL());
  }

  /**
   * Reads a key of the real registry into this object.
   *
   * <p>The key is exported with {@code reg export} to a temporary file, and that file is loaded.
   * The temporary file is deleted afterwards. As with every load, the content is added to what the
   * object holds already.
   *
   * @param registryKey the path of the key, for example {@code HKEY_CURRENT_USER\Software\ini4j}
   * @throws IOException if the {@code reg} command fails (the message is the text it wrote to its
   *     error output) or the exported file cannot be loaded
   * @throws UnsupportedOperationException if this is not Windows
   */
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  public void read(String registryKey) throws IOException {
    File tmp = createTempFile();

    try {
      regExport(registryKey, tmp);
      load(tmp);
    } finally {
      tmp.delete();
    }
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
    output.write(getVersion());
    output.write(getConfig().getLineSeparator());
    output.write(getConfig().getLineSeparator());
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
   * Writes this object to the real registry.
   *
   * <p>The content is stored to a temporary file, which is imported with {@code reg import}, and
   * then deleted. This changes the registry of the machine.
   *
   * @throws IOException if the {@code reg} command fails (the message is the text it wrote to its
   *     error output)
   * @throws UnsupportedOperationException if this is not Windows
   */
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  public void write() throws IOException {
    File tmp = createTempFile();

    try {
      store(tmp);
      regImport(tmp);
    } finally {
      tmp.delete();
    }
  }

  /**
   * Creates the handler that builds this object while a file is parsed.
   *
   * <p>A subclass can override it to change how the parsed events are turned into keys and values.
   *
   * @return a new builder for this object
   */
  protected IniHandler newBuilder() {
    return RegBuilder.newInstance(this);
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

  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("DM_DEFAULT_ENCODING")
  void exec(String[] args) throws IOException {
    Process proc = Runtime.getRuntime().exec(args);

    try {
      int status = proc.waitFor();

      if (status != 0) {
        Reader in = new InputStreamReader(proc.getErrorStream());
        char[] buff = new char[STDERR_BUFF_SIZE];
        int n;

        try {
          n = in.read(buff);
        } finally {
          in.close();
        }

        throw new IOException(new String(buff, 0, n).trim());
      }
    } catch (InterruptedException x) {
      throw (IOException) new InterruptedIOException().initCause(x);
    }
  }

  private File createTempFile() throws IOException {
    File ret = File.createTempFile(TMP_PREFIX, DEFAULT_SUFFIX);

    ret.deleteOnExit();

    return ret;
  }

  private void regExport(String registryKey, File file) throws IOException {
    requireWindows();
    exec(new String[] {"cmd", "/c", "reg", "export", registryKey, file.getAbsolutePath()});
  }

  private void regImport(File file) throws IOException {
    requireWindows();
    exec(new String[] {"cmd", "/c", "reg", "import", file.getAbsolutePath()});
  }

  private void requireWindows() {
    if (!WINDOWS) {
      throw new UnsupportedOperationException(
          "Unsupported operating system or runtime environment");
    }
  }
}
