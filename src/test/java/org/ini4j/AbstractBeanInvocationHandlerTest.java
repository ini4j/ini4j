/**
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

import junit.framework.Test;
import junit.framework.TestSuite;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import java.lang.reflect.Proxy;

///CLOVER:OFF
import java.util.HashMap;
import java.util.Map;

/**
 * JUnit test of AbstractBeanInvocationHandler class.
 */
public class AbstractBeanInvocationHandlerTest extends AbstractTestBase
{
    private static final String PROP_AGE = "age";
    private static final String PROP_HEIGHT = "height";

    /**
     * Instantiate test.
     *
     * @param testName name of the test
     */
    public AbstractBeanInvocationHandlerTest(String testName)
    {
        super(testName);
    }

    /**
     * Create test suite.
     *
     * @return new test suite
     */
    public static Test suite()
    {
        return new TestSuite(AbstractBeanInvocationHandlerTest.class);
    }

    public void testGetProperty() throws Exception
    {
        Map<String, String> map = new HashMap<String, String>();
        MapBeanHandler handler = new MapBeanHandler(map);
        Integer i = new Integer(23);

        map.put(PROP_AGE, "23");
        assertEquals(i, (Integer) handler.getProperty(PROP_AGE, Integer.class));
        assertTrue(handler.hasProperty(PROP_AGE));
        assertFalse(handler.hasProperty(null));
        map.put(PROP_AGE, "?.");
        assertEquals(null, handler.getProperty(PROP_AGE, Integer.class));
        assertEquals("?.", (String) handler.getProperty(PROP_AGE, String.class));
        handler = new MapBeanHandler(map)
        {
            @Override protected boolean hasPropertySpi(String property)
            {
                throw new UnsupportedOperationException();
            }
        };
        assertFalse(handler.hasProperty(PROP_AGE));
    }

    public void testMisc() throws Exception
    {
        Map<String, String> map = new HashMap<String, String>();
        MapBeanHandler handler = new MapBeanHandler(map);
        Dummy dummy = (Dummy) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Dummy.class }, handler);

        assertNull(handler.getProxy());

        // non existend method calls
        dummy.dummy();
        dummy.addDummy();
        dummy.removeDummy();
        assertSame(dummy, handler.getProxy());

        // boolean invoke
        map.put("dummy", "true");
        assertTrue(dummy.isDummy());

        // subclass should call fire methods any time
        // so null support reference should be not a problem
        handler.firePropertyChange(PROP_AGE, new Integer(1), new Integer(2));
        handler.fireVetoableChange(PROP_AGE, new Integer(1), new Integer(2));
    }

    /**
     * Test of newDwarfs method.
     *
     * @throws Exception on error
     */
    public void testNewDwarfs() throws Exception
    {
        doTestDwarfs(newDwarfs());
    }

    public void testPropertyChangeListener() throws Exception
    {
        class Listener implements PropertyChangeListener
        {
            int _counter;
            String _property;
            PropertyChangeEvent _event;

            Listener(String property)
            {
                _property = property;
            }

            @Override public void propertyChange(PropertyChangeEvent event)
            {
                if (_property.equals(event.getPropertyName()))
                {
                    _counter++;
                    _event = event;
                }
            }
        }

        Dwarf d = newDwarf();
        Listener l = new Listener(PROP_AGE);

        // test add and remove: invalid state should be OK
        d.removePropertyChangeListener(PROP_AGE, l);
        d.addPropertyChangeListener(PROP_AGE, l);
        d.addPropertyChangeListener(PROP_AGE, l);
        d.removePropertyChangeListener(PROP_AGE, l);
        d.removePropertyChangeListener(PROP_AGE, l);

        // check listener call
        d.setAge(23);
        d.addPropertyChangeListener(PROP_AGE, l);
        d.setAge(45);
        assertNotNull(l._event);
        assertEquals(23, ((Integer) l._event.getOldValue()).intValue());
        assertEquals(45, ((Integer) l._event.getNewValue()).intValue());

        // check listener call again
        d.setAge(2);
        d.setWeight(23.4);
        assertEquals(2, l._counter);
        d.removePropertyChangeListener(PROP_AGE, l);

        // should not run listener
        d.setAge(44);
        assertEquals(2, l._counter);

        // test remove listener> invalid state should be OK
        d.removePropertyChangeListener(PROP_AGE, l);
    }

    public void testSetProperty() throws Exception
    {
        Map<String, String> map = new HashMap<String, String>();
        MapBeanHandler handler = new MapBeanHandler(map);

        // very special case: set string property to non stirng value implies string conversion
        handler.setProperty(PROP_AGE, new Integer(23), String.class);
        assertEquals("23", handler.getProperty(PROP_AGE, String.class));
    }

    public void testVetoableChangeListener() throws Exception
    {
        class HeightCheck implements VetoableChangeListener
        {
            @Override public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException
            {
                if (PROP_HEIGHT.equals(event.getPropertyName()))
                {
                    if (((Double) event.getNewValue()) < 0.0)
                    {
                        throw new PropertyVetoException("invalid value", event);
                    }
                }
            }
        }

        Dwarf d = newDwarf();
        HeightCheck l = new HeightCheck();

        // test add and remove: invalid state should be OK
        d.removeVetoableChangeListener(PROP_HEIGHT, l);
        d.addVetoableChangeListener(PROP_HEIGHT, l);
        d.addVetoableChangeListener(PROP_HEIGHT, l);
        d.removeVetoableChangeListener(PROP_HEIGHT, l);
        d.removeVetoableChangeListener(PROP_HEIGHT, l);

        // set invalid value without lsitener
        d.setHeight(-2.0);
        d.setHeight(33.0);
        d.addVetoableChangeListener(PROP_HEIGHT, l);

        // set invalid value with listener
        try
        {
            d.setHeight(-3.4);
            fail();
        }
        catch (PropertyVetoException x)
        {
            assertEquals(33.0, d.getHeight());
        }

        // set valid value
        d.setHeight(44.0);
        assertEquals(44.0, d.getHeight());
        d.removeVetoableChangeListener(PROP_HEIGHT, l);

        // set invalid value without lsitener
        d.setHeight(-4.0);
        assertEquals(-4.0, d.getHeight());

        // test remove: invalid state should be OK
        d.removeVetoableChangeListener(PROP_HEIGHT, l);
    }

    static interface Dummy
    {
        boolean isDummy();

        void addDummy();

        void dummy();

        void removeDummy();
    }
}
