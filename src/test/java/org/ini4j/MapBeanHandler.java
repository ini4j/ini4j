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

import java.lang.reflect.Proxy;

import java.util.HashMap;
import java.util.Map;

class MapBeanHandler extends AbstractBeanInvocationHandler
{
    private Map<String, String> _map;

    MapBeanHandler(Map<String, String> map)
    {
        super();
        _map = map;
    }

    protected static <T> T newBean(Class<T> clazz)
    {
        return newBean(clazz, new HashMap<String, String>());
    }

    protected static <T> T newBean(Class<T> clazz, Map<String, String> map)
    {
        return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new MapBeanHandler(map)));
    }

    @Override protected Object getPropertySpi(String property, Class clazz)
    {
        return _map.get(property);
    }

    @Override protected void setPropertySpi(String property, Object value, Class clazz)
    {
        _map.put(property, value.toString());
    }

    @Override protected boolean hasPropertySpi(String property)
    {
        return _map.containsKey(property);
    }
}
