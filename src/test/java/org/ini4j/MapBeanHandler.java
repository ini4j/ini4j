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
