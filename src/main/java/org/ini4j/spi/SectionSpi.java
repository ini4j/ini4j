package org.ini4j.spi;

import org.ini4j.model.Section;

public interface SectionSpi extends Section
{
    IniSpi getIni();

    void setIni(IniSpi value);

    String getName();

    void setName(String value);
}
