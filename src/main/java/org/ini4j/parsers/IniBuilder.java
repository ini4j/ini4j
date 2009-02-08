package org.ini4j.parsers;

import org.ini4j.model.Ini;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import java.net.URL;

public interface IniBuilder
{
    Ini newIni();

    Ini parse(InputStream input) throws IOException, IniParseException;

    Ini parse(Reader input) throws IOException, IniParseException;

    Ini parse(URL input) throws IOException, IniParseException;

    void reset();
}
