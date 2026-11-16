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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.TimeZone;

/**
 * Converts text to Java values and connects beans with a store of properties.
 *
 * <p>It does the work behind {@link org.ini4j.OptionMap#get(Object, Class)}, {@link
 * org.ini4j.OptionMap#as(Class)}, {@link org.ini4j.OptionMap#from(Object)} and {@link
 * org.ini4j.OptionMap#to(Object)}. Properties are found with {@link java.beans.Introspector}, so
 * the property names are those of the Java Beans conventions.
 *
 * <p>The instance is created once, when the class is first used, with the provider lookup described
 * in {@link IniParser#newInstance()}, using the name {@code org.ini4j.spi.BeanTool}. A provider
 * that cannot be created makes the class initialization fail. The class has no state, so it can be
 * used from several threads.
 *
 * @see BeanAccess
 * @since 0.4.1
 */
public class BeanTool {
  private static final String PARSE_METHOD = "valueOf";
  private static final BeanTool INSTANCE = ServiceFinder.findService(BeanTool.class);

  /**
   * Returns the shared instance.
   *
   * @return the instance
   */
  public static final BeanTool getInstance() {
    return INSTANCE;
  }

  /**
   * Copies the values of a store into the properties of a bean.
   *
   * <p>Each writable property of the bean for which the store has a value is set. An array property
   * gets all the values, and any other property gets the last one, converted with {@link
   * #parse(String, Class)}. Other properties are not touched.
   *
   * @param bean the bean to change
   * @param props the store to read
   * @throws IllegalArgumentException if a property cannot be set; the message names the property
   */
  public void inject(Object bean, BeanAccess props) {
    for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass())) {
      try {
        Method method = pd.getWriteMethod();
        String name = pd.getName();

        if ((method != null) && (props.propLength(name) != 0)) {
          Object value;

          if (pd.getPropertyType().isArray()) {
            value =
                Array.newInstance(pd.getPropertyType().getComponentType(), props.propLength(name));
            for (int i = 0; i < props.propLength(name); i++) {
              Array.set(
                  value, i, parse(props.propGet(name, i), pd.getPropertyType().getComponentType()));
            }
          } else {
            value = parse(props.propGet(name), pd.getPropertyType());
          }

          method.invoke(bean, value);
        }
      } catch (Exception x) {
        throw (IllegalArgumentException)
            new IllegalArgumentException("Failed to set property: " + pd.getDisplayName(), x);
      }
    }
  }

  /**
   * Copies the properties of a bean into a store.
   *
   * <p>Each readable property, except {@code class}, whose value is not {@code null} is written as
   * the text of the value. The elements of an array property are added one by one after the
   * existing values, and any other value replaces the last one.
   *
   * @param props the store to change
   * @param bean the bean to read
   * @throws IllegalArgumentException if a property cannot be read; the message names the property
   */
  public void inject(BeanAccess props, Object bean) {
    for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass())) {
      try {
        Method method = pd.getReadMethod();

        if ((method != null) && !"class".equals(pd.getName())) {
          Object value = method.invoke(bean, (Object[]) null);

          if (value != null) {
            if (pd.getPropertyType().isArray()) {
              for (int i = 0; i < Array.getLength(value); i++) {
                Object v = Array.get(value, i);

                if ((v != null) && !v.getClass().equals(String.class)) {
                  v = v.toString();
                }

                props.propAdd(pd.getName(), (String) v);
              }
            } else {
              props.propSet(pd.getName(), value.toString());
            }
          }
        }
      } catch (Exception x) {
        throw new IllegalArgumentException("Failed to set property: " + pd.getDisplayName(), x);
      }
    }
  }

  /**
   * Converts a text to a value of a type.
   *
   * <p>A {@code null} text gives the {@linkplain #zero(Class) zero value} of the type. A primitive
   * type is parsed with the {@code valueOf} method of its wrapper, so any text other than {@code
   * true} (in any letter case) is {@code false} for a {@code boolean}. {@link String} is returned
   * as it is, and {@link Character} is the first character. {@link java.io.File}, {@link
   * java.net.URL}, {@link java.net.URI}, {@link Class} (by name) and {@link java.util.TimeZone} are
   * created from the text. Any other type must have a static {@code valueOf(String)} method.
   *
   * @param <T> the type to convert to
   * @param value the text, or {@code null}
   * @param clazz the type
   * @return the value
   * @throws IllegalArgumentException if {@code clazz} is {@code null}, or the text cannot be
   *     converted; the cause tells why
   * @throws StringIndexOutOfBoundsException if the text is empty and the type is {@code Character}
   */
  @SuppressWarnings("unchecked")
  public <T> T parse(String value, Class<T> clazz) throws IllegalArgumentException {
    if (clazz == null) {
      throw new IllegalArgumentException("null argument");
    }

    Object o = null;

    if (value == null) {
      o = zero(clazz);
    } else if (clazz.isPrimitive()) {
      o = parsePrimitiveValue(value, clazz);
    } else {
      if (clazz == String.class) {
        o = value;
      } else if (clazz == Character.class) {
        o = Character.valueOf(value.charAt(0));
      } else {
        o = parseSpecialValue(value, clazz);
      }
    }

    return (T) o;
  }

  /**
   * Creates an object of an interface that reads and writes a store.
   *
   * <p>The object is a dynamic proxy that is created with the context class loader of the calling
   * thread. The rules for the methods are described in {@link org.ini4j.OptionMap#as(Class)}.
   *
   * @param <T> the interface type
   * @param clazz the interface to implement
   * @param props the store behind the object
   * @return the proxy
   * @throws IllegalArgumentException if {@code clazz} is not an interface
   */
  public <T> T proxy(Class<T> clazz, BeanAccess props) {
    return clazz.cast(
        Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] {clazz},
            new BeanInvocationHandler(props)));
  }

  /**
   * Returns the value that stands for "nothing" for a type.
   *
   * @param <T> the type
   * @param clazz the type
   * @return {@code false}, {@code 0} or {@code '\0'} for a primitive type, and {@code null} for any
   *     other type, including {@code void}
   */
  @SuppressWarnings("unchecked")
  public <T> T zero(Class<T> clazz) {
    Object o = null;

    if (clazz.isPrimitive()) {
      if (clazz == Boolean.TYPE) {
        o = Boolean.FALSE;
      } else if (clazz == Byte.TYPE) {
        o = Byte.valueOf((byte) 0);
      } else if (clazz == Character.TYPE) {
        o = Character.valueOf('\0');
      } else if (clazz == Double.TYPE) {
        o = Double.valueOf(0.0);
      } else if (clazz == Float.TYPE) {
        o = Float.valueOf(0.0f);
      } else if (clazz == Integer.TYPE) {
        o = Integer.valueOf(0);
      } else if (clazz == Long.TYPE) {
        o = Long.valueOf(0L);
      } else if (clazz == Short.TYPE) {
        o = Short.valueOf((short) 0);
      }
    }

    return (T) o;
  }

  /**
   * Converts a text to a value of a type that is not a primitive, a string or a character.
   *
   * <p>A subclass can override it to support more types.
   *
   * @param value the text
   * @param clazz the type
   * @return the value
   * @throws IllegalArgumentException if the text cannot be converted; the cause tells why
   */
  protected Object parseSpecialValue(String value, Class<?> clazz) throws IllegalArgumentException {
    Object o;

    try {
      if (clazz == File.class) {
        o = new File(value);
      } else if (clazz == URL.class) {
        o = new URL(value);
      } else if (clazz == URI.class) {
        o = new URI(value);
      } else if (clazz == Class.class) {
        o = Class.forName(value);
      } else if (clazz == TimeZone.class) {
        o = TimeZone.getTimeZone(value);
      } else {

        // TODO handle constructor with String arg as converter from String
        // look for "valueOf" converter method
        Method parser = clazz.getMethod(PARSE_METHOD, new Class[] {String.class});

        o = parser.invoke(null, new Object[] {value});
      }
    } catch (Exception x) {
      throw (IllegalArgumentException) new IllegalArgumentException().initCause(x);
    }

    return o;
  }

  private PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
    try {
      return Introspector.getBeanInfo(clazz).getPropertyDescriptors();
    } catch (IntrospectionException x) {
      throw new IllegalArgumentException(x);
    }
  }

  private Object parsePrimitiveValue(String value, Class<?> clazz) throws IllegalArgumentException {
    Object o = null;

    try {
      if (clazz == Boolean.TYPE) {
        o = Boolean.valueOf(value);
      } else if (clazz == Byte.TYPE) {
        o = Byte.valueOf(value);
      } else if (clazz == Character.TYPE) {
        o = Character.valueOf(value.charAt(0));
      } else if (clazz == Double.TYPE) {
        o = Double.valueOf(value);
      } else if (clazz == Float.TYPE) {
        o = Float.valueOf(value);
      } else if (clazz == Integer.TYPE) {
        o = Integer.valueOf(value);
      } else if (clazz == Long.TYPE) {
        o = Long.valueOf(value);
      } else if (clazz == Short.TYPE) {
        o = Short.valueOf(value);
      }
    } catch (Exception x) {
      throw (IllegalArgumentException) new IllegalArgumentException().initCause(x);
    }

    return o;
  }

  static class BeanInvocationHandler extends AbstractBeanInvocationHandler {
    private final BeanAccess _backend;

    BeanInvocationHandler(BeanAccess backend) {
      _backend = backend;
    }

    @Override
    protected Object getPropertySpi(String property, Class<?> clazz) {
      Object ret = null;

      if (clazz.isArray()) {
        int length = _backend.propLength(property);

        if (length != 0) {
          String[] all = new String[length];

          for (int i = 0; i < all.length; i++) {
            all[i] = _backend.propGet(property, i);
          }

          ret = all;
        }
      } else {
        ret = _backend.propGet(property);
      }

      return ret;
    }

    @Override
    protected void setPropertySpi(String property, Object value, Class<?> clazz) {
      if (clazz.isArray()) {
        _backend.propDel(property);
        for (int i = 0; i < Array.getLength(value); i++) {
          _backend.propAdd(property, Array.get(value, i).toString());
        }
      } else {
        _backend.propSet(property, value.toString());
      }
    }

    @Override
    protected boolean hasPropertySpi(String property) {
      return _backend.propLength(property) != 0;
    }
  }
}
