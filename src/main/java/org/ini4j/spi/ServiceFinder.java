/*
 * Copyright 2023 ini4j GitHub Organization
 *
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
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * JDK JAR Services API alap� service keres� oszt�ly.
 *
 * @author Szkiba Iv�n
 * @version $Name:  $
 */
final class ServiceFinder
{
    private static final String SERVICES_PATH = "META-INF/services/";

    private ServiceFinder()
    {
    }

    /**
     * Service objektum keres�s �s p�ld�nyos�t�s
     *
     * a JDK JAR specifik�ci�ban defini�lt <B>Services API</B>-nak
     * megfelel�en service oszt�ly keres�s, majd pedig p�ld�ny k�pz�s a context
     * ClassLoader seg�ts�g�vel.</p><p>
     * Az implement�l� oszt�ly n�v keres�se a <CODE>serviceId</CODE> nev�
     * system property vizsg�lat�val kezd�dik. Amennyiben nincs ilyen
     * property, �gy a keres�s a
     * <CODE>/META-INF/services/<I>serviceId</I></CODE> nev� file tartalm�val
     * folytat�dik. Amennyiben nincs ilyen nev� file, �gy a param�terk�nt �tadott
     * <CODE>defaultService</CODE> lesz az oszt�ly neve.</p><p>
     * A fenti keres�st k�vet�en t�rt�nik a p�ld�ny k�pz�s. A visszat�r�si
     * �rt�k mindig egy val�di objektum, l�v�n minden hiba exception-t gener�l.
     * @param <T> type
     * @param clazz keresett oszt�ly/service neve
     * @throws IllegalArgumentException keres�si vagy p�ld�nyos�t�si hiba eset�n
     * @return a keresett oszt�ly implement�l� objektum
     */
    static <T> T findService(Class<T> clazz)
    {
        try
        {

            // ez a cast nem lenne szükséges, de úgy a ClassCastException csak a hívónál jön...
            return clazz.cast(findServiceClass(clazz).newInstance());
        }
        catch (Exception x)
        {
            throw (IllegalArgumentException) new IllegalArgumentException("Provider " + clazz.getName() + " could not be instantiated: " + x)
              .initCause(x);
        }
    }

    @SuppressWarnings(Warnings.UNCHECKED)
    static <T> Class<? extends T> findServiceClass(Class<T> clazz) throws IllegalArgumentException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String serviceClassName = findServiceClassName(clazz.getName());
        Class<T> ret = clazz;

        if (serviceClassName != null)
        {
            try
            {
                ret = (Class<T>) ((classLoader == null) ? Class.forName(serviceClassName) : classLoader.loadClass(serviceClassName));
            }
            catch (ClassNotFoundException x)
            {
                throw (IllegalArgumentException) new IllegalArgumentException("Provider " + serviceClassName + " not found").initCause(x);
            }
        }

        return ret;
    }

    static String findServiceClassName(String serviceId) throws IllegalArgumentException
    {
        String serviceClassName = null;

        // Use the system property first
        try
        {
            String systemProp = System.getProperty(serviceId);

            if (systemProp != null)
            {
                serviceClassName = systemProp;
            }
        }
        catch (SecurityException x)
        {
            assert true;
        }

        if (serviceClassName == null)
        {
            serviceClassName = loadLine(SERVICES_PATH + serviceId);
        }

        return serviceClassName;
    }

    private static String loadLine(String servicePath)
    {
        String ret = null;

        // try to find services in CLASSPATH
        try
        {
            InputStream is = null;
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            if (classLoader == null)
            {
                is = ClassLoader.getSystemResourceAsStream(servicePath);
            }
            else
            {
                is = classLoader.getResourceAsStream(servicePath);
            }

            if (is != null)
            {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line = rd.readLine();

                rd.close();
                if (line != null)
                {
                    line = line.trim();
                    if (line.length() != 0)
                    {
                        ret = line.split("\\s|#")[0];
                    }
                }
            }
        }
        catch (Exception x)
        {
            assert true;
        }

        return ret;
    }
}
