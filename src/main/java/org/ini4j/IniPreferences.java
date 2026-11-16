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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * A {@link java.util.prefs.Preferences} tree on top of an {@link Ini}.
 *
 * <p>The root node is the {@code Ini} itself. Its child nodes are the sections, and the keys of a
 * section node are its options. A section named {@code a/b} (with the default path separator) is
 * the child {@code b} of the section {@code a}. The root node has no keys: {@code put} and {@code
 * remove} throw an {@link UnsupportedOperationException}, and {@code get} returns the default
 * value.
 *
 * <p>Asking for a node that does not exist creates the section in the {@code Ini} at once. All
 * changes stay in memory: {@code flush} and {@code sync} do nothing, so nothing is written to a
 * file. To keep the changes, store the {@code Ini}, which you hold if you created this object with
 * {@link #IniPreferences(Ini)}.
 *
 * <p>The nodes use the locking of {@link java.util.prefs.AbstractPreferences}. The {@link Ini}
 * itself is not synchronized.
 *
 * <p>This example reads and changes a value:
 *
 * <pre>{@code
 * Ini ini = new Ini(new File("dwarfs.ini"));
 * Preferences prefs = new IniPreferences(ini);
 * Preferences bashful = prefs.node("bashful");
 *
 * int age = bashful.getInt("age", -1);
 * bashful.putDouble("weight", 55.6);
 * ini.store();
 * }</pre>
 *
 * @see IniPreferencesFactory
 * @since 0.3.0
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
public class IniPreferences extends AbstractPreferences {

  /** A shared empty array of strings. */
  private static final String[] EMPTY = {};

  /** The {@code Ini} that holds the data. */
  private final Ini _ini;

  /**
   * Creates the root node on top of an {@link Ini}.
   *
   * @param ini the {@code Ini} that holds the data; it is used directly, not copied
   */
  public IniPreferences(Ini ini) {
    super(null, "");
    _ini = ini;
  }

  /**
   * Creates the root node on top of a new {@link Ini} that is loaded from a reader.
   *
   * @param input the reader that provides the INI data
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the data is not valid
   */
  public IniPreferences(Reader input) throws IOException, InvalidFileFormatException {
    super(null, "");
    _ini = new Ini(input);
  }

  /**
   * Creates the root node on top of a new {@link Ini} that is loaded from a stream.
   *
   * @param input the stream that provides the INI data
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the data is not valid
   */
  public IniPreferences(InputStream input) throws IOException, InvalidFileFormatException {
    super(null, "");
    _ini = new Ini(input);
  }

  /**
   * Creates the root node on top of a new {@link Ini} that is loaded from a URL.
   *
   * @param input the location of the INI data
   * @throws IOException if reading fails
   * @throws InvalidFileFormatException if the data is not valid
   */
  public IniPreferences(URL input) throws IOException, InvalidFileFormatException {
    super(null, "");
    _ini = new Ini(input);
  }

  /**
   * Returns the {@link Ini} that holds the data.
   *
   * @return the {@code Ini}, not a copy
   */
  protected Ini getIni() {
    return _ini;
  }

  /**
   * Not supported. The root node has no keys.
   *
   * <p>{@link java.util.prefs.Preferences#get(String, String)} catches the exception, so it returns
   * the default value for every key of the root node.
   *
   * @param key the key
   * @return nothing, the method always throws
   * @throws UnsupportedOperationException always
   */
  @Override
  protected String getSpi(String key) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the names of the child nodes, which are the sections that are not below another
   * section.
   *
   * <p>A section is below another one if its name contains the path separator of the {@link Config}
   * of the {@link Ini}.
   *
   * @return the names of the top level sections
   * @throws BackingStoreException never thrown by this implementation
   */
  @Override
  protected String[] childrenNamesSpi() throws BackingStoreException {
    List<String> names = new ArrayList<String>();

    for (String name : _ini.keySet()) {
      if (name.indexOf(_ini.getPathSeparator()) < 0) {
        names.add(name);
      }
    }

    return names.toArray(EMPTY);
  }

  /**
   * Returns the child node for a section.
   *
   * <p>If the {@link Ini} has no such section yet, it is added at once, and the node is reported as
   * new.
   *
   * @param name the name of the section
   * @return the node of the section
   */
  @Override
  protected SectionPreferences childSpi(String name) {
    Ini.Section sec = _ini.get(name);
    boolean isNew = sec == null;

    if (isNew) {
      sec = _ini.add(name);
    }

    return new SectionPreferences(this, sec, isNew);
  }

  /**
   * Does nothing. The changes stay in the {@link Ini} in memory and are not written anywhere.
   *
   * @throws BackingStoreException never thrown by this implementation
   */
  @Override
  protected void flushSpi() throws BackingStoreException {
    assert true;
  }

  /**
   * Returns no keys. The root node has no keys.
   *
   * @return an empty array
   * @throws BackingStoreException never thrown by this implementation
   */
  @Override
  protected String[] keysSpi() throws BackingStoreException {
    return EMPTY;
  }

  /**
   * Not supported. The root node has no keys.
   *
   * @param key the key
   * @param value the value
   * @throws UnsupportedOperationException always
   */
  @Override
  protected void putSpi(String key, String value) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Not supported. The root node cannot be removed.
   *
   * @throws UnsupportedOperationException always
   * @throws BackingStoreException never thrown by this implementation
   */
  @Override
  protected void removeNodeSpi() throws BackingStoreException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Not supported. The root node has no keys.
   *
   * @param key the key
   * @throws UnsupportedOperationException always
   */
  @Override
  protected void removeSpi(String key) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Does nothing. The data is not read again from anywhere.
   *
   * @throws BackingStoreException never thrown by this implementation
   */
  @Override
  protected void syncSpi() throws BackingStoreException {
    assert true;
  }

  /**
   * The node of one section.
   *
   * <p>The keys of the node are the option names of the section. {@code get} returns the last value
   * of an option with its variables resolved, {@code put} replaces the last value and {@code
   * remove} removes all the values. The child nodes are the sections below this one in the section
   * tree, and a child that does not exist is added to the {@link Ini} when it is requested.
   * Removing the node removes the section from the {@link Ini}.
   *
   * @since 0.3.0
   */
  protected class SectionPreferences extends AbstractPreferences {

    /** The section of this node. */
    private final Ini.Section _section;

    /**
     * Creates a node for a section.
     *
     * @param parent the parent node
     * @param section the section of this node
     * @param isNew whether the node is new, which is the case if the section did not exist before
     */
    SectionPreferences(AbstractPreferences parent, Ini.Section section, boolean isNew) {
      super(parent, section.getSimpleName());
      _section = section;
      newNode = isNew;
    }

    /**
     * Calls {@code flush()} of the parent node, which does nothing.
     *
     * @throws BackingStoreException never thrown by this implementation
     */
    @Override
    public void flush() throws BackingStoreException {
      parent().flush();
    }

    /**
     * Calls {@code sync()} of the parent node, which does nothing.
     *
     * @throws BackingStoreException never thrown by this implementation
     */
    @Override
    public void sync() throws BackingStoreException {
      parent().sync();
    }

    /**
     * Returns the last value of an option, with variables resolved.
     *
     * @param key the option name
     * @return the value, or {@code null} if the section has no such option
     * @throws CircularReferenceException if the variables are nested deeper than {@link
     *     Config#getMaxResolveDepth()}; {@link java.util.prefs.Preferences#get(String, String)}
     *     catches it and returns the default value
     */
    @Override
    protected String getSpi(String key) {
      return _section.fetch(key);
    }

    /**
     * Returns the names of the child nodes, which are the direct sub-sections of the section.
     *
     * @return the names of the sub-sections, relative to this section
     * @throws BackingStoreException never thrown by this implementation
     */
    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
      return _section.childrenNames();
    }

    /**
     * Returns the child node for a sub-section.
     *
     * <p>If the sub-section does not exist yet, it is added to the {@link Ini} at once, and the
     * node is reported as new.
     *
     * @param name the name of the sub-section, relative to this section
     * @return the node of the sub-section
     */
    @Override
    protected SectionPreferences childSpi(String name) throws UnsupportedOperationException {
      Ini.Section child = _section.getChild(name);
      boolean isNew = child == null;

      if (isNew) {
        child = _section.addChild(name);
      }

      return new SectionPreferences(this, child, isNew);
    }

    /**
     * Does nothing. The changes stay in the {@link Ini} in memory and are not written anywhere.
     *
     * @throws BackingStoreException never thrown by this implementation
     */
    @Override
    protected void flushSpi() throws BackingStoreException {
      assert true;
    }

    /**
     * Returns the keys of the node.
     *
     * @return the option names of the section
     * @throws BackingStoreException never thrown by this implementation
     */
    @Override
    protected String[] keysSpi() throws BackingStoreException {
      return _section.keySet().toArray(EMPTY);
    }

    /**
     * Replaces the last value of an option, or adds the option.
     *
     * @param key the option name
     * @param value the new value
     */
    @Override
    protected void putSpi(String key, String value) {
      _section.put(key, value);
    }

    /**
     * Removes the sections that have the name of this node from the {@link Ini}.
     *
     * @throws BackingStoreException never thrown by this implementation
     */
    @Override
    protected void removeNodeSpi() throws BackingStoreException {
      _ini.remove(_section);
    }

    /**
     * Removes all the values of an option.
     *
     * @param key the option name
     */
    @Override
    protected void removeSpi(String key) {
      _section.remove(key);
    }

    /**
     * Does nothing. The data is not read again from anywhere.
     *
     * @throws BackingStoreException never thrown by this implementation
     */
    @Override
    protected void syncSpi() throws BackingStoreException {
      assert true;
    }
  }
}
