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
///CLOVER:OFF

/**
 * JUnit test of ServiceFinder class.
 */
public class ServiceFinderTest extends AbstractTestBase
{
    static final String DUMMY = "dummy";
    static final String DUMMY_SERVICE = "org.ini4j.Dummy";
    static final String BAD_CONFIG_SERVICE = "org.ini4j.BadConfig";
    static final String EMPTY_CONFIG_SERVICE = "org.ini4j.EmptyConfig";
    static final String DUMMY_IMPL = "DummyImpl";

    /**
     * Instantiate test.
     *
     * @param testName name of the test
     */
    public ServiceFinderTest(String testName)
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
        return new TestSuite(ServiceFinderTest.class);
    }

    /**
     * Test of findService method.
     *
     * @throws Exception on error
     */
    public void testFindService() throws Exception
    {
        boolean flag = false;

        System.setProperty(IniParser.class.getName(), AbstractTestBase.class.getName());
        try
        {
            ServiceFinder.findService(IniParser.class);
        }
        catch (IllegalArgumentException x)
        {
            flag = true;
        }

        // System.clearProperty(IniParser.SERVICE_ID); missing in 1.4
        System.getProperties().remove(IniParser.class.getName());
        assertTrue(flag);
    }

    /**
     * Test of findServiceClass method.
     *
     * @throws Exception on error
     */
    public void testFindServiceClass() throws Exception
    {
        boolean flag = false;

        System.setProperty(IniParser.class.getName(), DUMMY);
        try
        {
            ServiceFinder.findServiceClass(IniParser.class);
        }
        catch (IllegalArgumentException x)
        {
            flag = true;
        }

        // System.clearProperty(IniParser.SERVICE_ID); missing in 1.4
        System.getProperties().remove(IniParser.class.getName());
        assertTrue(flag);
    }

    /**
     * Test of findServiceClassName method.
     *
     * @throws Exception on error
     */
    public void testFindServiceClassName() throws Exception
    {
        boolean flag = false;

        try
        {
            ServiceFinder.findServiceClassName(IniParser.class.getName(), null);
        }
        catch (IllegalArgumentException x)
        {
            flag = true;
        }

        assertTrue(flag);
        System.setProperty(IniParser.class.getName(), DUMMY);
        assertEquals(DUMMY, ServiceFinder.findServiceClassName(IniParser.class.getName(), IniParser.class.getName()));

        // System.clearProperty(IniParser.SERVICE_ID); missing in 1.4
        System.getProperties().remove(IniParser.class.getName());
        assertEquals(IniParser.class.getName(), ServiceFinder.findServiceClassName(IniParser.class.getName(), IniParser.class.getName()));
        assertEquals(DUMMY_IMPL, ServiceFinder.findServiceClassName(DUMMY_SERVICE, ""));
        assertEquals(DUMMY, ServiceFinder.findServiceClassName(BAD_CONFIG_SERVICE, DUMMY));
        assertEquals(DUMMY, ServiceFinder.findServiceClassName(EMPTY_CONFIG_SERVICE, DUMMY));
    }
}
