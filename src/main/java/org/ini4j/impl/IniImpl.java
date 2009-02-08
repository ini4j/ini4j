package org.ini4j.impl;

import org.ini4j.spi.IniSpi;
import org.ini4j.spi.SectionSpi;

public class IniImpl implements IniSpi
{
    private SectionSpi _rootSection;

    @Override public SectionSpi getRootSection()
    {
        return _rootSection;
    }

    @Override public void setRootSection(SectionSpi value)
    {
        _rootSection = value;
    }

    @Override public String resolve(String line, SectionSpi currentSection)
    {
        throw new UnsupportedOperationException();
    }
}
