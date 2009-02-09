/*
 * Copyright 2005 [ini4j] Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ini4j;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;

import java.net.URI;
import java.net.URL;

import java.util.TimeZone;

/**
 * JUnit test of Bean class.
 */
public class BeanTest
{
    private static final float DELTA = 0.00000001f;

    @SuppressWarnings("empty-statement")
    @Test public void testParseValue() throws Exception
    {
        String input = "6";
        int value = 6;

        assertEquals(value, ((Byte) Bean.parseValue(input, byte.class)).byteValue());
        assertEquals(value, ((Short) Bean.parseValue(input, short.class)).shortValue());
        assertEquals(value, ((Integer) Bean.parseValue(input, int.class)).intValue());
        assertEquals(value, ((Long) Bean.parseValue(input, long.class)).longValue());
        assertEquals((float) value, ((Float) Bean.parseValue(input, float.class)).floatValue(), DELTA);
        assertEquals((double) value, ((Double) Bean.parseValue(input, double.class)).doubleValue(), DELTA);
        assertFalse(((Boolean) Bean.parseValue(input, boolean.class)));
        assertEquals('6', ((Character) Bean.parseValue(input, char.class)).charValue());

        // parse null mean zero
        assertEquals(0, ((Byte) Bean.parseValue(null, byte.class)).byteValue());

        // parse to null class mean exception
        try
        {
            Bean.parseValue(input, null);
            fail();
        }
        catch (IllegalArgumentException x)
        {
            ;
        }

        // invalid primitive value mean exception
        try
        {
            Bean.parseValue("?", int.class);
            fail();
        }
        catch (IllegalArgumentException x)
        {
            ;
        }

        // standard, but not primitive types
        assertSame(input, Bean.parseValue(input, String.class));
        assertEquals(new Character('6'), ((Character) Bean.parseValue(input, Character.class)));
        assertEquals(new Byte(input), ((Byte) Bean.parseValue(input, Byte.class)));

        // special values
        input = "http://www.ini4j.org";
        assertEquals(new URL(input), Bean.parseValue(input, URL.class));
        assertEquals(new URI(input), Bean.parseValue(input, URI.class));
        assertEquals(new File(input), Bean.parseValue(input, File.class));
        input = "Europe/Budapest";
        assertEquals(input, ((TimeZone) Bean.parseValue(input, TimeZone.class)).getID());
        input = "java.lang.String";
        assertEquals(String.class, (Class) Bean.parseValue(input, Class.class));

        // invalid value should throw IllegalArgumentException
        try
        {
            Bean.parseValue("", URL.class);
        }
        catch (IllegalArgumentException x)
        {
            ;
        }
    }

    @Test public void testZero() throws Exception
    {
        assertEquals(null, Bean.zero(Object.class));
        assertEquals(0, ((Byte) Bean.zero(byte.class)).byteValue());
        assertEquals(0, ((Short) Bean.zero(short.class)).shortValue());
        assertEquals(0, ((Integer) Bean.zero(int.class)).intValue());
        assertEquals(0, ((Long) Bean.zero(long.class)).longValue());
        assertEquals(0.0f, ((Float) Bean.zero(float.class)).floatValue(), DELTA);
        assertEquals(0.0, ((Double) Bean.zero(double.class)).doubleValue(), DELTA);
        assertNotNull((Bean.zero(boolean.class)));
        assertFalse(((Boolean) Bean.zero(boolean.class)));
        assertEquals('\0', ((Character) Bean.zero(char.class)).charValue());
    }
}
