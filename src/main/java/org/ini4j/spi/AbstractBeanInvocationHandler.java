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

import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * The base class for the handlers of the dynamic proxies that show a store as a bean.
 *
 * <p>The handler maps the methods of the proxy to properties by the first letters of the method
 * name: {@code getX} and {@code isX} read the property {@code x}, {@code setX} writes it, and
 * {@code hasX} tells whether it exists. The property name is the rest of the method name changed as
 * {@link java.beans.Introspector#decapitalize(String)} does. The methods {@code
 * addPropertyChangeListener}, {@code addVetoableChangeListener} and the two matching {@code remove}
 * methods take the property name and a listener. Only the start of the name is checked, so a method
 * such as {@code issue()} is a read of the property {@code sue}.
 *
 * <p>A subclass tells how to read, write and test a property in the store. The text from the store
 * is converted to the type of the property here. A read that fails with an exception gives the
 * {@linkplain BeanTool#zero(Class) zero value} of the type, not an exception. A write first asks
 * the vetoable listeners of the property, if there are some, then writes, then tells the property
 * change listeners. The old value for the listeners is read only if there are listeners.
 *
 * <p>The methods of {@link Object} are not handled. A call of {@code toString()} returns {@code
 * null}, {@code equals} throws a {@link NullPointerException}, and {@code hashCode} throws a {@link
 * ClassCastException} because its name starts with {@code has}.
 *
 * <p><strong>Thread safety.</strong> The methods that read or write a property, and the ones that
 * change the listeners, are synchronized on the handler, so two threads do not run them at the same
 * time. The store behind it is not protected.
 *
 * @see BeanTool
 * @since 0.4.1
 */
public abstract class AbstractBeanInvocationHandler implements InvocationHandler {
  private static final String PROPERTY_CHANGE_LISTENER = "PropertyChangeListener";
  private static final String VETOABLE_CHANGE_LISTENER = "VetoableChangeListener";
  private static final String ADD_PREFIX = "add";
  private static final String READ_PREFIX = "get";
  private static final String REMOVE_PREFIX = "remove";
  private static final String READ_BOOLEAN_PREFIX = "is";
  private static final String WRITE_PREFIX = "set";
  private static final String HAS_PREFIX = "has";

  private enum Prefix {
    READ(READ_PREFIX),
    READ_BOOLEAN(READ_BOOLEAN_PREFIX),
    WRITE(WRITE_PREFIX),
    ADD_CHANGE(ADD_PREFIX + PROPERTY_CHANGE_LISTENER),
    ADD_VETO(ADD_PREFIX + VETOABLE_CHANGE_LISTENER),
    REMOVE_CHANGE(REMOVE_PREFIX + PROPERTY_CHANGE_LISTENER),
    REMOVE_VETO(REMOVE_PREFIX + VETOABLE_CHANGE_LISTENER),
    HAS(HAS_PREFIX);
    private int _len;
    private String _value;

    Prefix(String value) {
      _value = value;
      _len = value.length();
    }

    public static Prefix parse(String str) {
      Prefix ret = null;

      for (Prefix p : values()) {
        if (str.startsWith(p.getValue())) {
          ret = p;

          break;
        }
      }

      return ret;
    }

    public String getTail(String input) {
      return Introspector.decapitalize(input.substring(_len));
    }

    public String getValue() {
      return _value;
    }
  }

  private PropertyChangeSupport _pcSupport;
  private Object _proxy;
  private VetoableChangeSupport _vcSupport;

  /**
   * Handles a call of a method of the proxy, as described in the description of this class.
   *
   * @param proxy the proxy
   * @param method the method that was called
   * @param args the arguments of the call
   * @return the value for a read or a {@code has} method, and {@code null} otherwise
   * @throws PropertyVetoException if a vetoable listener rejects the new value of a write
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws PropertyVetoException {
    Object ret = null;
    Prefix prefix = Prefix.parse(method.getName());

    if (prefix != null) {
      String tail = prefix.getTail(method.getName());

      updateProxy(proxy);
      switch (prefix) {
        case READ:
          ret = getProperty(prefix.getTail(method.getName()), method.getReturnType());
          break;

        case READ_BOOLEAN:
          ret = getProperty(prefix.getTail(method.getName()), method.getReturnType());
          break;

        case WRITE:
          setProperty(tail, args[0], method.getParameterTypes()[0]);
          break;

        case HAS:
          ret = Boolean.valueOf(hasProperty(prefix.getTail(method.getName())));
          break;

        case ADD_CHANGE:
          addPropertyChangeListener((String) args[0], (PropertyChangeListener) args[1]);
          break;

        case ADD_VETO:
          addVetoableChangeListener((String) args[0], (VetoableChangeListener) args[1]);
          break;

        case REMOVE_CHANGE:
          removePropertyChangeListener((String) args[0], (PropertyChangeListener) args[1]);
          break;

        case REMOVE_VETO:
          removeVetoableChangeListener((String) args[0], (VetoableChangeListener) args[1]);
          break;

        default:
          break;
      }
    }

    return ret;
  }

  /**
   * Reads a property from the store.
   *
   * @param property the property name
   * @param clazz the type of the property
   * @return the value; text is converted to {@code clazz} by the caller, and for an array type the
   *     caller accepts an array of text. {@code null} means that there is no value
   */
  protected abstract Object getPropertySpi(String property, Class<?> clazz);

  /**
   * Writes a property to the store.
   *
   * @param property the property name
   * @param value the new value
   * @param clazz the type of the property
   */
  protected abstract void setPropertySpi(String property, Object value, Class<?> clazz);

  /**
   * Tells whether the store has a property.
   *
   * @param property the property name
   * @return {@code true} if the property has a value
   */
  protected abstract boolean hasPropertySpi(String property);

  /**
   * Reads a property and converts it to a type.
   *
   * @param property the property name
   * @param clazz the type of the property
   * @return the value, or the zero value of the type if there is none or the read or the conversion
   *     fails
   */
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("REC_CATCH_EXCEPTION")
  protected synchronized Object getProperty(String property, Class<?> clazz) {
    Object o;

    try {
      o = getPropertySpi(property, clazz);
      if (o == null) {
        o = zero(clazz);
      } else if (clazz.isArray() && (o instanceof String[]) && !clazz.equals(String[].class)) {
        String[] str = (String[]) o;

        o = Array.newInstance(clazz.getComponentType(), str.length);
        for (int i = 0; i < str.length; i++) {
          Array.set(o, i, parse(str[i], clazz.getComponentType()));
        }
      } else if ((o instanceof String) && !clazz.equals(String.class)) {
        o = parse((String) o, clazz);
      }
    } catch (Exception x) {
      o = zero(clazz);
    }

    return o;
  }

  /**
   * Writes a property and notifies the listeners.
   *
   * <p>A value of a type other than {@link String} for a {@code String} property is stored as its
   * text.
   *
   * @param property the property name
   * @param value the new value
   * @param clazz the type of the property
   * @throws PropertyVetoException if a vetoable listener rejects the new value
   */
  protected synchronized void setProperty(String property, Object value, Class<?> clazz)
      throws PropertyVetoException {
    boolean pc = (_pcSupport != null) && _pcSupport.hasListeners(property);
    boolean vc = (_vcSupport != null) && _vcSupport.hasListeners(property);
    Object oldVal = null;
    Object newVal =
        ((value != null) && clazz.equals(String.class) && !(value instanceof String))
            ? value.toString()
            : value;

    if (pc || vc) {
      oldVal = getProperty(property, clazz);
    }

    if (vc) {
      fireVetoableChange(property, oldVal, value);
    }

    setPropertySpi(property, newVal, clazz);
    if (pc) {
      firePropertyChange(property, oldVal, value);
    }
  }

  /**
   * Returns the proxy that this handler serves.
   *
   * @return the proxy, or {@code null} if no method that the handler maps to a property or a
   *     listener has been called yet
   */
  protected synchronized Object getProxy() {
    return _proxy;
  }

  /**
   * Adds a listener for the changes of a property.
   *
   * @param property the property name
   * @param listener the listener
   */
  protected synchronized void addPropertyChangeListener(
      String property, PropertyChangeListener listener) {
    if (_pcSupport == null) {
      _pcSupport = new PropertyChangeSupport(_proxy);
    }

    _pcSupport.addPropertyChangeListener(property, listener);
  }

  /**
   * Adds a listener that can veto the changes of a property.
   *
   * @param property the property name
   * @param listener the listener
   */
  protected synchronized void addVetoableChangeListener(
      String property, VetoableChangeListener listener) {
    if (_vcSupport == null) {
      _vcSupport = new VetoableChangeSupport(_proxy);
    }

    _vcSupport.addVetoableChangeListener(property, listener);
  }

  /**
   * Tells the property change listeners about a change.
   *
   * @param property the property name
   * @param oldValue the old value
   * @param newValue the new value
   */
  protected synchronized void firePropertyChange(
      String property, Object oldValue, Object newValue) {
    if (_pcSupport != null) {
      _pcSupport.firePropertyChange(property, oldValue, newValue);
    }
  }

  /**
   * Asks the vetoable change listeners about a change.
   *
   * @param property the property name
   * @param oldValue the old value
   * @param newValue the new value
   * @throws PropertyVetoException if a listener rejects the change
   */
  protected synchronized void fireVetoableChange(String property, Object oldValue, Object newValue)
      throws PropertyVetoException {
    if (_vcSupport != null) {
      _vcSupport.fireVetoableChange(property, oldValue, newValue);
    }
  }

  /**
   * Tells whether a property has a value.
   *
   * @param property the property name
   * @return {@code true} if it has; {@code false} if it has not, or the test fails
   */
  protected synchronized boolean hasProperty(String property) {
    boolean ret;

    try {
      ret = hasPropertySpi(property);
    } catch (Exception x) {
      ret = false;
    }

    return ret;
  }

  /**
   * Converts a text to a type with {@link BeanTool#parse(String, Class)}.
   *
   * @param value the text
   * @param clazz the type
   * @return the value
   * @throws IllegalArgumentException if the text cannot be converted
   */
  protected Object parse(String value, Class<?> clazz) throws IllegalArgumentException {
    return BeanTool.getInstance().parse(value, clazz);
  }

  /**
   * Removes a listener for the changes of a property.
   *
   * @param property the property name
   * @param listener the listener
   */
  protected synchronized void removePropertyChangeListener(
      String property, PropertyChangeListener listener) {
    if (_pcSupport != null) {
      _pcSupport.removePropertyChangeListener(property, listener);
    }
  }

  /**
   * Removes a listener that can veto the changes of a property.
   *
   * @param property the property name
   * @param listener the listener
   */
  protected synchronized void removeVetoableChangeListener(
      String property, VetoableChangeListener listener) {
    if (_vcSupport != null) {
      _vcSupport.removeVetoableChangeListener(property, listener);
    }
  }

  /**
   * Returns the zero value of a type with {@link BeanTool#zero(Class)}.
   *
   * @param clazz the type
   * @return the zero value
   */
  protected Object zero(Class<?> clazz) {
    return BeanTool.getInstance().zero(clazz);
  }

  private synchronized void updateProxy(Object value) {
    if (_proxy == null) {
      _proxy = value;
    }
  }
}
