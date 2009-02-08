/*
 * Copyright 2005 [ini4j] Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ini4j.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import java.net.URL;

public interface IniParser
{
    char COMMENT = ';';
    char OPERATOR = '=';
    char SECTION_BEGIN = '[';
    char SECTION_END = ']';

    Object getProperty(String name) throws IniNotSupportedException, IniNotRecognizedException;

    void setProperty(String name, Object value) throws IniNotSupportedException, IniNotRecognizedException;

    void parse(InputStream input, IniHandler handler) throws IOException, IniParseException;

    void parse(Reader input, IniHandler handler) throws IOException, IniParseException;

    void parse(URL input, IniHandler handler) throws IOException, IniParseException;

    void reset();
}
