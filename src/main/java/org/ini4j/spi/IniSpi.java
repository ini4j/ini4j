package org.ini4j.spi;

import org.ini4j.model.Ini;

public interface IniSpi extends Ini
{
    SectionSpi getRootSection();

    void setRootSection(SectionSpi value);

    String resolve(String line, SectionSpi currentSection);
}
