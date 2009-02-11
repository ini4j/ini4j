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

import java.net.URI;

import java.util.prefs.Preferences;

public class TestHelper
{
    public static String DWARFS_INI = "org/ini4j/dwarfs.ini";
    public static String DWARFS_XML = "org/ini4j/dwarfs.xml";
    public static final float DELTA = 0.00000001f;

    protected static void assertEquals(Dwarf expected, Dwarf actual)
    {
        Assert.assertEquals(expected.getAge(), actual.getAge());
        Assert.assertEquals(expected.getHeight(), actual.getHeight(), DELTA);
        Assert.assertEquals(expected.getWeight(), actual.getWeight(), DELTA);
        Assert.assertEquals(expected.getHomePage().toString(), actual.getHomePage().toString());
    }

    protected static void assertEquals(Dwarf expected, Ini.Section actual)
    {
        Assert.assertEquals("" + expected.getAge(), actual.fetch("age"));
        Assert.assertEquals("" + expected.getHeight(), actual.fetch("height"));
        Assert.assertEquals("" + expected.getWeight(), actual.fetch("weight"));
        Assert.assertEquals("" + expected.getHomePage(), actual.fetch("homePage"));
    }

    protected static void assertEquals(Dwarf expected, Preferences actual)
    {
        Assert.assertEquals("" + expected.getAge(), actual.get("age", null));
        Assert.assertEquals("" + expected.getHeight(), actual.get("height", null));
        Assert.assertEquals("" + expected.getWeight(), actual.get("weight", null));
        Assert.assertEquals("" + expected.getHomePage(), actual.get("homePage", null));
    }

    protected void doTestDwarfs(Dwarfs dwarfs) throws Exception
    {
        Dwarf d;

        d = dwarfs.getBashful();
        assertHasProperties(d);
        Assert.assertEquals(45.7, d.getWeight(), DELTA);
        Assert.assertEquals(98.8, d.getHeight(), DELTA);
        Assert.assertEquals(67, d.getAge());
        Assert.assertEquals("http://snowwhite.tale/~bashful", d.getHomePage().toString());
        d = dwarfs.getDoc();
        assertHasProperties(d);
        Assert.assertEquals(49.5, d.getWeight(), DELTA);
        Assert.assertEquals(87.7, d.getHeight(), DELTA);
        Assert.assertEquals(63, d.getAge());
        Assert.assertEquals("http://doc.dwarfs", d.getHomePage().toString());
        d = dwarfs.getDopey();
        assertHasProperties(d);
        Assert.assertEquals(dwarfs.getBashful().getWeight(), d.getWeight());
        Assert.assertEquals(dwarfs.getDoc().getHeight(), d.getHeight());
        Assert.assertEquals(23, d.getAge());
        Assert.assertEquals("http://dopey.snowwhite.tale/", d.getHomePage().toString());
        d = dwarfs.getGrumpy();
        assertHasProperties(d);
        Assert.assertEquals(65.3, d.getWeight(), DELTA);
        Assert.assertEquals(dwarfs.getDopey().getHeight(), d.getHeight(), DELTA);
        Assert.assertEquals(76, d.getAge());
        Assert.assertEquals("http://snowwhite.tale/~grumpy/", d.getHomePage().toString());
        d = dwarfs.getHappy();
        assertHasProperties(d);
        Assert.assertEquals(56.4, d.getWeight(), DELTA);
        Assert.assertEquals(77.66, d.getHeight(), DELTA);
        Assert.assertEquals(99, d.getAge());
        Assert.assertEquals("http://happy.smurf", d.getHomePage().toString());
        d = dwarfs.getSleepy();
        assertHasProperties(d);
        Assert.assertEquals(76.11, d.getWeight(), DELTA);
        Assert.assertEquals(87.78, d.getHeight(), DELTA);
        Assert.assertEquals(121, d.getAge());
        Assert.assertEquals("http://snowwhite.tale/~sleepy", d.getHomePage().toString());
        d = dwarfs.getSneezy();
        assertHasProperties(d);
        Assert.assertEquals(69.7, d.getWeight(), DELTA);
        Assert.assertEquals(76.88, d.getHeight(), DELTA);
        Assert.assertEquals(64, d.getAge());
        Assert.assertEquals(dwarfs.getHappy().getHomePage().toString() + "/~sneezy", d.getHomePage().toString());
    }

    protected Ini loadDwarfs() throws Exception
    {
        return new Ini(getClass().getClassLoader().getResourceAsStream(DWARFS_INI));
    }

    protected Dwarf newDwarf()
    {
        return new DwarfBean();
    }

    protected Dwarfs newDwarfs() throws Exception
    {
        DwarfsBean dwarfs = new DwarfsBean();
        Dwarf d;

        d = new DwarfBean();
        d.setWeight(45.7);
        d.setHeight(98.8);
        d.setAge(67);
        d.setHomePage(new URI("http://snowwhite.tale/~bashful"));
        dwarfs.setBashful(d);
        d = new DwarfBean();
        d.setWeight(49.5);
        d.setHeight(87.7);
        d.setAge(63);
        d.setHomePage(new URI("http://doc.dwarfs"));
        dwarfs.setDoc(d);
        d = new DwarfBean();
        d.setWeight(dwarfs.getBashful().getWeight());
        d.setHeight(dwarfs.getDoc().getHeight());
        d.setAge(23);
        d.setHomePage(new URI("http://dopey.snowwhite.tale/"));
        dwarfs.setDopey(d);
        d = new DwarfBean();
        d.setWeight(65.3);
        d.setHeight(dwarfs.getDopey().getHeight());
        d.setAge(76);
        d.setHomePage(new URI("http://snowwhite.tale/~grumpy/"));
        dwarfs.setGrumpy(d);
        d = new DwarfBean();
        d.setWeight(56.4);
        d.setHeight(77.66);
        d.setAge(99);
        d.setHomePage(new URI("http://happy.smurf"));
        dwarfs.setHappy(d);
        d = new DwarfBean();
        d.setWeight(76.11);
        d.setHeight(87.78);
        d.setAge(121);
        d.setHomePage(new URI("http://snowwhite.tale/~sleepy"));
        dwarfs.setSleepy(d);
        d = new DwarfBean();
        d.setWeight(69.7);
        d.setHeight(76.88);
        d.setAge(64);
        d.setHomePage(new URI(dwarfs.getHappy().getHomePage().toString() + "/~sneezy"));
        dwarfs.setSneezy(d);

        return dwarfs;
    }

    private void assertHasProperties(Dwarf dwarf)
    {
        assertTrue(dwarf.hasWeight());
        assertTrue(dwarf.hasHeight());
        assertTrue(dwarf.hasAge());
        assertTrue(dwarf.hasHomePage());
    }
}
