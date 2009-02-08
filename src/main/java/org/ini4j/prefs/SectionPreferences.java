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
package org.ini4j.prefs;

import org.ini4j.model.Section;

import org.ini4j.spi.SectionSpi;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

public class SectionPreferences extends AbstractPreferences
{

    /** frequently used empty String array */
    private static final String[] EMPTY = {};

    /** underlaying <code>Ini</code> implementation */
    private Section _section;

    /**
     * Constructs a new preferences node on top of <code>Section</code> instance.
     *
     * @param section underlaying <code>Section</code> instance
     */
    public SectionPreferences(Section section)
    {
        this(null, section, false);
    }

    /**
     * Constructs a new SectionPreferences instance on top of Section instance.
     *
     * @param parent parent preferences node
     * @parem section underlaying Section instance
     * @param isNew indicate is this a new node or already existing one
     */
    protected SectionPreferences(SectionPreferences parent, Section section, boolean isNew)
    {
        super(parent, ((SectionSpi) section).getName());
        _section = section;
        newNode = isNew;
    }

    protected Section getParentSection()
    {
        return ((SectionPreferences) parent())._section;
    }

    /**
     * Provide access to underlaying {@link org.ini4j.model.Section} implementation.
     *
     * @return <code>Section</code> implementation
     */
    protected Section getSection()
    {
        return _section;
    }

    /**
     * Implements the <CODE>getSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#getSpi(String)}.
     *
     * @return if the value associated with the specified key at this preference node, or null if there is no association for this key, or the association cannot be determined at this time.
     * @param key key to getvalue for
     */
    @Override protected String getSpi(String key)
    {
        return _section.fetch(key);
    }

    /**
     * Implements the <CODE>childrenNamesSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#childrenNamesSpi()}.
     * @return an array containing the names of the children of this preference node.
     * @throws BackingStoreException if this operation cannot be completed due to a failure in the backing store, or inability to communicate with it.
     */
    @Override protected String[] childrenNamesSpi() throws BackingStoreException
    {
        return _section.getSections().keySet().toArray(EMPTY);
    }

    /**
     * Implements the <CODE>childSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#childSpi(String)}.
     * @param name child name
     * @return child node
     */
    @Override protected AbstractPreferences childSpi(String name)
    {
        Section sec = _section.getSections().get(name);
        boolean isNew = sec == null;

        if (isNew)
        {
            _section.addSection(name);
        }

        return new SectionPreferences(this, sec, isNew);
    }

    /**
     * Implements the <CODE>flushSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#flushSpi()}.
     *
     * This implementation does nothing.
     *
     * @throws BackingStoreException if this operation cannot be completed due to a failure in the backing store, or inability to communicate with it.
     */
    @Override
    @SuppressWarnings("empty-statement")
    protected void flushSpi() throws BackingStoreException
    {
        ;
    }

    /**
     * Implements the <CODE>keysSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#keysSpi()}.
     *
     * @return keys
     * @throws BackingStoreException if this operation cannot be completed due to a failure in the backing store, or inability to communicate with it.
     */
    @Override protected String[] keysSpi() throws BackingStoreException
    {
        return _section.keySet().toArray(EMPTY);
    }

    /**
     * Implements the <CODE>putSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#putSpi(String,String)}.
     *
     * @param key key to set value for
     * @param value new value for key
     */
    @Override protected void putSpi(String key, String value)
    {

        // nem lehet többszörös érték !
        _section.put(key, value);
    }

    /**
     * Implements the <CODE>removeNodeSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#removeNodeSpi()}.
     *
     * @throws BackingStoreException this implementation never throws this exception
     */
    @Override protected void removeNodeSpi() throws BackingStoreException
    {
        getParentSection().getSections().remove(((SectionSpi) _section).getName());
    }

    /**
     * Implements the <CODE>removeSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#removeSpi(String)}.
     * @param key key to remove
     */
    @Override protected void removeSpi(String key)
    {
        _section.remove(key);
    }

    /**
     * Implements the <CODE>syncSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#syncSpi()}.
     *
     * This implementation does nothing.
     *
     * @throws BackingStoreException if this operation cannot be completed due to a failure in the backing store, or inability to communicate with it.
     */
    @Override
    @SuppressWarnings("empty-statement")
    protected void syncSpi() throws BackingStoreException
    {
        ;
    }
}
