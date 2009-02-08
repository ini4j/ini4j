package org.ini4j.parsers;

import java.util.ServiceLoader;

public abstract class IniBuilderFactory
{
    public abstract boolean getFeature(String name) throws IniNotSupportedException, IniNotRecognizedException;

    public abstract void setFeature(String name, boolean value) throws IniNotSupportedException, IniNotRecognizedException;

    public abstract Object getProperty(String name) throws IniNotSupportedException, IniNotRecognizedException;

    public abstract void setProperty(String name, Object value) throws IniNotSupportedException, IniNotRecognizedException;

    public abstract IniBuilder newIniBuilder();

    public static IniBuilderFactory newInstance()
    {
        return ServiceLoader.load(IniBuilderFactory.class).iterator().next();
    }
}
