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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * JUnit test of Ini class.
 */
public class IniTest
{
    private static final String UNICODE_STRING = "áÁéÉíÍóÓöÖőŐúÚüÜűŰ-ÄÖÜäöü";
    private TestHelper _helper;

    @Before public void setUp()
    {
        _helper = new TestHelper();
    }

    /**
     * Test of bean related methods.
     *
     * @throws Exception on error
     */
    @Test public void testBeanInterface() throws Exception
    {
        Dwarfs exp = _helper.newDwarfs();
        Ini ini = _helper.loadDwarfs();
        Ini.Section sec = ini.get("doc");
        Dwarfs dwarfs = ini.to(Dwarfs.class);
        Dwarf bean = _helper.newDwarf();

        sec.to(bean);
        assertEquals(exp.getDoc(), bean);
        sec.clear();
        sec.from(bean);
        assertEquals(exp.getDoc(), sec);
    }

    /**
     * Test of load method.
     *
     * @throws Exception on error
     */
    @Test public void testLoad() throws Exception
    {
        Ini ini = _helper.loadDwarfs();

        _helper.doTestDwarfs(ini.to(Dwarfs.class));
        ini = new Ini(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(TestHelper.DWARFS_INI)));
        _helper.doTestDwarfs(ini.to(Dwarfs.class));
        ini = new Ini(getClass().getClassLoader().getResource(TestHelper.DWARFS_INI));
        _helper.doTestDwarfs(ini.to(Dwarfs.class));
    }

    /**
     * Test of loadFromXML method.
     *
     * @throws Exception on error
     */
    @Test public void testLoadFromXML() throws Exception
    {
        Ini ini = new Ini();

        ini.loadFromXML(getClass().getClassLoader().getResourceAsStream(TestHelper.DWARFS_XML));
        _helper.doTestDwarfs(ini.to(Dwarfs.class));
        ini.loadFromXML(getClass().getClassLoader().getResource(TestHelper.DWARFS_XML));
        _helper.doTestDwarfs(ini.to(Dwarfs.class));
    }

    /**
     * Test of remove method.
     *
     * @throws Exception on error
     */
    @Test public void testRemove() throws Exception
    {
        Ini ini = _helper.loadDwarfs();

        ini.remove(ini.get("doc"));
        assertNull(ini.get("doc"));
    }

    /**
     * Test of resolve method.
     *
     * @throws Exception on error
     */
    @Test public void testResolve() throws Exception
    {
        Ini ini = _helper.loadDwarfs();
        Ini.Section doc = ini.get("doc");
        Dwarfs dwarfs = ini.to(Dwarfs.class);
        StringBuilder buffer;
        String input;

        // other sections's value
        input = "${happy/weight}";
        buffer = new StringBuilder(input);

        ini.resolve(buffer, doc);
        assertEquals("" + dwarfs.getHappy().getWeight(), buffer.toString());

        // same sections's value
        input = "${height}";
        buffer = new StringBuilder(input);

        ini.resolve(buffer, doc);
        assertEquals("" + dwarfs.getDoc().getHeight(), buffer.toString());

        // system property
        input = "${@prop/user.home}";
        buffer = new StringBuilder(input);

        ini.resolve(buffer, doc);
        assertEquals(System.getProperty("user.home"), buffer.toString());

        // system environment
        input = "${@env/path}";
        buffer = new StringBuilder(input);
        try
        {
            ini.resolve(buffer, doc);
            assertEquals(System.getenv("path"), buffer.toString());
        }
        catch (Error e)
        {
            // retroweaver + JDK 1.4 throws Error on getenv
        }

        // unknown variable
        input = "${no such name}";
        buffer = new StringBuilder(input);

        ini.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // unknown section's unknown variable
        input = "${no such section/no such name}";
        buffer = new StringBuilder(input);

        ini.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // other section's unknown variable
        input = "${happy/no such name}";
        buffer = new StringBuilder(input);

        ini.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // small input
        input = "${";
        buffer = new StringBuilder(input);

        ini.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // incorrect references
        input = "${doc/weight";
        buffer = new StringBuilder(input);

        ini.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // empty references
        input = "jim${}";
        buffer = new StringBuilder(input);

        ini.resolve(buffer, doc);
        assertEquals(input, buffer.toString());

        // escaped references
        input = "${happy/weight}";
        buffer = new StringBuilder(input);

        ini.resolve(buffer, doc);
        assertEquals("" + dwarfs.getHappy().getWeight(), buffer.toString());
        input = "\\" + input;
        buffer = new StringBuilder(input);

        assertEquals(input, buffer.toString());
    }

    /**
     * Test of store method.
     *
     * @throws Exception on error
     */
    @Test public void testStore() throws Exception
    {
        Ini ini = _helper.loadDwarfs();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        ini.store(buffer);
        Ini dup = new Ini();

        dup.load(new ByteArrayInputStream(buffer.toByteArray()));
        _helper.doTestDwarfs(dup.to(Dwarfs.class));
        buffer = new ByteArrayOutputStream();
        ini.store(new OutputStreamWriter(buffer));
        dup = new Ini();
        dup.load(new InputStreamReader(new ByteArrayInputStream(buffer.toByteArray())));
        _helper.doTestDwarfs(dup.to(Dwarfs.class));
    }

    /**
     * Test of storeToXML method.
     *
     * @throws Exception on error
     */
    @Test public void testStoreToXML() throws Exception
    {
        Ini ini = _helper.loadDwarfs();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        ini.storeToXML(buffer);
        Ini dup = new Ini();

        dup.loadFromXML(new ByteArrayInputStream(buffer.toByteArray()));
        _helper.doTestDwarfs(dup.to(Dwarfs.class));
        buffer = new ByteArrayOutputStream();
        ini.storeToXML(new OutputStreamWriter(buffer));
        dup = new Ini();
        dup.loadFromXML(new InputStreamReader(new ByteArrayInputStream(buffer.toByteArray())));
        _helper.doTestDwarfs(dup.to(Dwarfs.class));
    }

    @Test public void testToBean() throws Exception
    {
        Ini ini = _helper.loadDwarfs();
        Ini.Section sec = ini.get("doc");
        Dwarf doc = sec.to(Dwarf.class);
    }

    @Test public void testUnicode() throws Exception
    {
        Ini orig = new Ini();
        Ini.Section bashful = orig.add(Dwarfs.PROP_BASHFUL);

        bashful.put(Dwarf.PROP_HOME_PAGE, UNICODE_STRING);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        orig.store(out);
        Ini saved = new Ini(new ByteArrayInputStream(out.toByteArray()));
        Ini.Section bashfulSaved = saved.get(Dwarfs.PROP_BASHFUL);

        assertEquals(bashful.get(Dwarf.PROP_HOME_PAGE), bashfulSaved.get(Dwarf.PROP_HOME_PAGE));
    }

    static interface Tale extends Dwarfs
    {
        Snowwhite getSnowwhite();

        void setSnowwhite(Snowwhite s);

        boolean hasSnowwhite();

        static interface Snowwhite
        {
            String getEmail();

            void setEmail(String email);
        }
    }
}
