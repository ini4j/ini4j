package org.ini4j.parsers;

import java.util.ServiceLoader;

public abstract class IniParserFactory
{
    public abstract boolean getFeature(String name) throws IniNotSupportedException, IniNotRecognizedException;

    public abstract void setFeature(String name, boolean value) throws IniNotSupportedException, IniNotRecognizedException;

    public abstract IniParser newIniParser();

    public static IniParserFactory newInstance()
    {
        return ServiceLoader.load(IniParserFactory.class).iterator().next();
    }
}
