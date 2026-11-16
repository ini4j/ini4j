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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

/**
 * An object that can load its content from, and store it to, a file, a stream, a reader or a
 * writer, or a URL.
 *
 * <p>An object can have an associated file, which is used by {@link #load()} and {@link #store()}.
 * The constructors of {@link Ini}, {@link Options} and {@link Reg} that take a {@link File} set it.
 *
 * <p>Loading does not clear the object first. It adds what it reads to the existing content, and
 * the {@link Config} flags such as {@link Config#isMultiSection()} and {@link
 * Config#isMultiOption()} decide what happens to names that are already there. When loading has
 * read the input to its end, it closes the input. If loading fails before that, the input is left
 * open.
 *
 * <p>Storing writes the content as the {@link Config} flags allow. For example, comments, empty
 * sections, options without a value and, when {@link Config#isMultiOption()} is off, all but one
 * value of an option can be left out (see that method for the value that is kept). The methods that
 * take an {@link OutputStream} or a {@link Writer} flush it but do not close it. {@link
 * #store(File)} creates the file or overwrites it, and closes it. The text is encoded with {@link
 * Config#getFileEncoding()}. The writers used for storing are {@link java.io.PrintWriter}s, which
 * do not throw an exception when a write fails. An {@link IOException} is mostly raised when a file
 * cannot be opened.
 *
 * @see Configurable
 * @since 0.5.0
 */
public interface Persistable {
  /**
   * Returns the file used by {@link #load()} and {@link #store()}.
   *
   * @return the file, or {@code null} if none is set
   */
  File getFile();

  /**
   * Sets the file used by {@link #load()} and {@link #store()}.
   *
   * @param value the file, or {@code null} to have none
   */
  void setFile(File value);

  /**
   * Loads the content from the associated file.
   *
   * @throws java.io.FileNotFoundException if there is no associated file, or the file does not
   *     exist
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  void load() throws IOException, InvalidFileFormatException;

  /**
   * Loads the content from a stream.
   *
   * <p>The bytes are decoded with {@link Config#getFileEncoding()}.
   *
   * @param input the stream to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  void load(InputStream input) throws IOException, InvalidFileFormatException;

  /**
   * Loads the content from a reader.
   *
   * @param input the reader to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  void load(Reader input) throws IOException, InvalidFileFormatException;

  /**
   * Loads the content from a file.
   *
   * <p>This does not change the associated file.
   *
   * @param input the file to read
   * @throws IOException if the file cannot be read
   * @throws InvalidFileFormatException if the content is not valid
   */
  void load(File input) throws IOException, InvalidFileFormatException;

  /**
   * Loads the content from a URL.
   *
   * @param input the location to read
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the content is not valid
   */
  void load(URL input) throws IOException, InvalidFileFormatException;

  /**
   * Stores the content to the associated file.
   *
   * @throws java.io.FileNotFoundException if there is no associated file, or it cannot be opened
   * @throws IOException if writing fails
   */
  void store() throws IOException;

  /**
   * Stores the content to a stream.
   *
   * <p>The text is encoded with {@link Config#getFileEncoding()}. The stream is flushed, not
   * closed.
   *
   * @param output the stream to write
   * @throws IOException if writing fails
   */
  void store(OutputStream output) throws IOException;

  /**
   * Stores the content to a writer.
   *
   * <p>The writer is flushed, not closed.
   *
   * @param output the writer to write
   * @throws IOException if writing fails
   */
  void store(Writer output) throws IOException;

  /**
   * Stores the content to a file.
   *
   * <p>This does not change the associated file.
   *
   * @param output the file to write; it is created or overwritten
   * @throws IOException if the file cannot be opened or written
   */
  void store(File output) throws IOException;
}
