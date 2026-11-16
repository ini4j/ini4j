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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Finds the provider of a service and creates it.
 *
 * <p>It follows the JAR Services API of the JDK, with a system property as an extra first source.
 * Every {@code newInstance()} method of the SPI classes uses it, and so do the shared instances of
 * {@link EscapeTool}, {@link RegEscapeTool} and {@link BeanTool}. The public description of the
 * lookup is in {@link IniParser#newInstance()}.
 *
 * @since 0.4.1
 */
final class ServiceFinder {
  private static final String SERVICES_PATH = "META-INF/services/";

  private ServiceFinder() {}

  /**
   * Finds the provider class of a service and creates an instance of it.
   *
   * <p>The name of the provider class is looked up in this order: the system property that is named
   * like the service class, then the first line of the resource {@code META-INF/services/} and the
   * name of the service class (see {@link #findServiceClassName(String)}). If neither gives a name,
   * the service class itself is the provider. The class is loaded with the context class loader of
   * the current thread, and created with its constructor that has no arguments.
   *
   * <p>The result is never {@code null}: every failure throws an exception.
   *
   * @param <T> the type of the service
   * @param clazz the service class, which is also the default provider
   * @return a new instance of the provider
   * @throws IllegalArgumentException if the provider cannot be found or created, for any reason
   *     that is an {@link Exception}; the cause tells why
   */
  static <T> T findService(Class<T> clazz) {
    try {

      // The cast is not needed, but without it a ClassCastException would show up only at the
      // caller.
      return clazz.cast(findServiceClass(clazz).getDeclaredConstructor().newInstance());
    } catch (Exception x) {
      throw (IllegalArgumentException)
          new IllegalArgumentException(
                  "Provider " + clazz.getName() + " could not be instantiated: " + x)
              .initCause(x);
    }
  }

  /**
   * Finds the provider class of a service.
   *
   * <p>The name comes from {@link #findServiceClassName(String)}. If there is no name, the result
   * is the service class itself. A name is loaded with the context class loader of the current
   * thread, or with {@link Class#forName(String)} if the thread has none.
   *
   * @param <T> the type of the service
   * @param clazz the service class
   * @return the provider class
   * @throws IllegalArgumentException if the named class cannot be found
   * @throws LinkageError if the JVM fails to load or link the class; it is not wrapped
   */
  @SuppressWarnings(Warnings.UNCHECKED)
  static <T> Class<? extends T> findServiceClass(Class<T> clazz) throws IllegalArgumentException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String serviceClassName = findServiceClassName(clazz.getName());
    Class<T> ret = clazz;

    if (serviceClassName != null) {
      try {
        ret =
            (Class<T>)
                ((classLoader == null)
                    ? Class.forName(serviceClassName)
                    : classLoader.loadClass(serviceClassName));
      } catch (ClassNotFoundException x) {
        throw (IllegalArgumentException)
            new IllegalArgumentException("Provider " + serviceClassName + " not found")
                .initCause(x);
      }
    }

    return ret;
  }

  /**
   * Finds the name of the provider class of a service.
   *
   * <p>The first source that gives a value wins. The sources are the system property that has the
   * service name as its name, and the first line of the resource {@code META-INF/services/}
   * followed by the service name. A system property that cannot be read because of a security
   * manager is skipped. See {@link #loadLine(String)} for the resource.
   *
   * @param serviceId the fully qualified name of the service class
   * @return the name of the provider class, or {@code null} if there is none
   */
  static String findServiceClassName(String serviceId) throws IllegalArgumentException {
    String serviceClassName = null;

    // Use the system property first
    try {
      String systemProp = System.getProperty(serviceId);

      if (systemProp != null) {
        serviceClassName = systemProp;
      }
    } catch (SecurityException x) {
      assert true;
    }

    if (serviceClassName == null) {
      serviceClassName = loadLine(SERVICES_PATH + serviceId);
    }

    return serviceClassName;
  }

  /**
   * Reads the provider name from a service resource.
   *
   * <p>Only the first line of the resource is read, with the UTF-8 character set, using the context
   * class loader of the current thread (or the system class loader if there is none). The line is
   * trimmed and cut at the first white space or {@code #}. A blank first line gives {@code null},
   * and a line that starts with {@code #} gives an empty text. Any failure while reading is ignored
   * and gives {@code null}.
   *
   * @param servicePath the path of the resource
   * @return the name from the first line, or {@code null}
   */
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("REC_CATCH_EXCEPTION")
  private static String loadLine(String servicePath) {
    String ret = null;
    InputStream is = null;

    // try to find services in CLASSPATH
    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      if (classLoader == null) {
        is = ClassLoader.getSystemResourceAsStream(servicePath);
      } else {
        is = classLoader.getResourceAsStream(servicePath);
      }

      if (is != null) {
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line;

        try {
          line = rd.readLine();
        } finally {
          rd.close();
        }

        if (line != null) {
          line = line.trim();
          if (line.length() != 0) {
            ret = line.split("\\s|#")[0];
          }
        }
      }
    } catch (Exception x) {
      assert true;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          assert true;
        }
      }
    }

    return ret;
  }
}
