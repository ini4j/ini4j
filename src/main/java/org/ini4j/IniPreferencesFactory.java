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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * A {@link PreferencesFactory} that serves the {@code java.util.prefs} API from INI files.
 *
 * <p>The user root and the system root are two separate {@link IniPreferences} trees. Each is built
 * from an {@link Ini} loaded from a location that is read from the system property {@link
 * #KEY_USER} or {@link #KEY_SYSTEM}. If the property is not set, the same key is looked up in the
 * file {@link #PROPERTIES}, which is searched on the class path with the context class loader of
 * the calling thread. If there is no location, the tree starts empty.
 *
 * <p>A location with a scheme, such as {@code file:/etc/app.ini}, is used as a URL. A location
 * without a scheme is the name of a resource on the class path. A location that cannot be read
 * throws an {@link IllegalArgumentException} when the root is first requested. The changes made
 * through the preferences stay in memory, because {@code flush} and {@code sync} do not write
 * anything (see {@link IniPreferences}).
 *
 * <p>To let {@link Preferences#userRoot()} use it, name the class in the system property {@code
 * java.util.prefs.PreferencesFactory} before the preferences API is used for the first time,
 * because the factory is chosen only once:
 *
 * <pre>{@code
 * System.setProperty("java.util.prefs.PreferencesFactory", "org.ini4j.IniPreferencesFactory");
 * System.setProperty("org.ini4j.prefs.user", "file:/home/joe/app.ini");
 *
 * Preferences prefs = Preferences.userRoot().node("happy");
 * }</pre>
 *
 * <p><strong>Thread safety.</strong> {@link #systemRoot()} and {@link #userRoot()} are
 * synchronized. Each root is created on the first call and every later call returns the same
 * object.
 *
 * @see IniPreferences
 * @since 0.3.0
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
public class IniPreferencesFactory implements PreferencesFactory {
  /**
   * The name of the class path resource that can hold the locations, as a properties file with the
   * keys {@link #KEY_USER} and {@link #KEY_SYSTEM}.
   */
  public static final String PROPERTIES = "ini4j.properties";

  /**
   * The key of the location of the user preferences, used as a system property and as a key in
   * {@link #PROPERTIES}.
   */
  public static final String KEY_USER = "org.ini4j.prefs.user";

  /**
   * The key of the location of the system preferences, used as a system property and as a key in
   * {@link #PROPERTIES}.
   */
  public static final String KEY_SYSTEM = "org.ini4j.prefs.system";

  private Preferences _system;
  private Preferences _user;

  /**
   * Returns the root of the system preferences.
   *
   * <p>The root is created on the first call, from the location named by {@link #KEY_SYSTEM}.
   *
   * @return the same root on every call
   * @throws IllegalArgumentException if the location cannot be read or is not valid on the first
   *     call
   */
  @Override
  public synchronized Preferences systemRoot() {
    if (_system == null) {
      _system = newIniPreferences(KEY_SYSTEM);
    }

    return _system;
  }

  /**
   * Returns the root of the user preferences.
   *
   * <p>The root is created on the first call, from the location named by {@link #KEY_USER}.
   *
   * @return the same root on every call
   * @throws IllegalArgumentException if the location cannot be read or is not valid on the first
   *     call
   */
  @Override
  public synchronized Preferences userRoot() {
    if (_user == null) {
      _user = newIniPreferences(KEY_USER);
    }

    return _user;
  }

  String getIniLocation(String key) {
    String location = Config.getSystemProperty(key);

    if (location == null) {
      try {
        Properties props = new Properties();

        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES));
        location = props.getProperty(key);
      } catch (Exception x) {
        assert true;
      }
    }

    return location;
  }

  URL getResource(String location) throws IllegalArgumentException {
    try {
      URI uri = new URI(location);
      URL url;

      if (uri.getScheme() == null) {
        url = Thread.currentThread().getContextClassLoader().getResource(location);
      } else {
        url = uri.toURL();
      }

      return url;
    } catch (Exception x) {
      throw (IllegalArgumentException) new IllegalArgumentException().initCause(x);
    }
  }

  InputStream getResourceAsStream(String location) throws IllegalArgumentException {
    try {
      return getResource(location).openStream();
    } catch (Exception x) {
      throw (IllegalArgumentException) new IllegalArgumentException().initCause(x);
    }
  }

  Preferences newIniPreferences(String key) {
    Ini ini = new Ini();
    String location = getIniLocation(key);

    if (location != null) {
      try {
        ini.load(getResourceAsStream(location));
      } catch (Exception x) {
        throw (IllegalArgumentException) new IllegalArgumentException().initCause(x);
      }
    }

    return new IniPreferences(ini);
  }
}
