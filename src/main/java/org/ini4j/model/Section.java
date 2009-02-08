package org.ini4j.model;

public interface Section extends MultiMap<String, String>
{
    MultiMap<String, Section> getSections();

    Section addSection(String name);

    String fetch(Object key);

    String fetch(Object key, int index);

    <T> T to(Class<T> clazz);
}
