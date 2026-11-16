/*
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
/**
 * The parsers, formatters and handlers that read and write the files, and the tools they use.
 *
 * <p>Reading and writing work with events. A parser ({@link org.ini4j.spi.IniParser}, {@link
 * org.ini4j.spi.OptionsParser}) reads a file and calls the methods of a handler ({@link
 * org.ini4j.spi.IniHandler}, {@link org.ini4j.spi.OptionsHandler}) for each section, option and
 * comment. A builder ({@link org.ini4j.spi.IniBuilder}, {@link org.ini4j.spi.OptionsBuilder},
 * {@link org.ini4j.spi.RegBuilder}) is a handler that fills an {@link org.ini4j.Ini}, an {@link
 * org.ini4j.Options} or a {@link org.ini4j.Reg} from these events. A formatter ({@link
 * org.ini4j.spi.IniFormatter}, {@link org.ini4j.spi.OptionsFormatter}) is a handler that writes the
 * events as text. When an object is stored, it sends its content to a formatter as the same events.
 * The order of the events is described in {@link org.ini4j.spi.IniHandler} and {@link
 * org.ini4j.spi.OptionsHandler}.
 *
 * <p>The classes that do this work can be replaced. The factory methods, such as {@link
 * org.ini4j.spi.IniParser#newInstance()}, look for the class to create in the system property that
 * is named like the class, then in the first line of the file {@code META-INF/services/} and the
 * name of the class, and use the class itself if there is no such name. The provider is a subclass
 * with a public constructor without arguments. The full description is in {@link
 * org.ini4j.spi.IniParser#newInstance()}.
 *
 * <p>The package also has the tools for the escape forms ({@link org.ini4j.spi.EscapeTool}, {@link
 * org.ini4j.spi.WinEscapeTool}, {@link org.ini4j.spi.RegEscapeTool}) and for beans ({@link
 * org.ini4j.spi.BeanTool}, {@link org.ini4j.spi.BeanAccess}, {@link
 * org.ini4j.spi.AbstractBeanInvocationHandler}).
 */
package org.ini4j.spi;
