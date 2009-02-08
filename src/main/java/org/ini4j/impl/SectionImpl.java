package org.ini4j.impl;

import org.ini4j.model.MultiMap;
import org.ini4j.model.Section;

import org.ini4j.spi.IniSpi;
import org.ini4j.spi.SectionSpi;

public class SectionImpl extends MultiMapImpl<String, String> implements SectionSpi
{
    private IniSpi _ini;
    private String _name;
    private MultiMap<String, Section> _sections;

    @Override public IniSpi getIni()
    {
        return _ini;
    }

    @Override public void setIni(IniSpi value)
    {
        _ini = value;
    }

    @Override public String getName()
    {
        return _name;
    }

    @Override public void setName(String value)
    {
        _name = value;
    }

    @Override public synchronized MultiMap<String, Section> getSections()
    {
        if (_sections == null)
        {
            _sections = new MultiMapImpl<String, Section>();
        }

        return _sections;
    }

    @Override public Section addSection(String name)
    {
        SectionSpi section = newSectionSpi();

        section.setName(name);
        section.setIni(getIni());
        getSections().add(name, section);

        return section;
    }

    @Override public String fetch(Object key)
    {
        return getIni().resolve(get(key), this);
    }

    @Override public String fetch(Object key, int index)
    {
        return getIni().resolve(get(key, index), this);
    }

    @Override public synchronized <T> T to(Class<T> clazz)
    {
        throw new UnsupportedOperationException();
    }

    protected SectionSpi newSectionSpi()
    {
        return new SectionImpl();
    }
}
