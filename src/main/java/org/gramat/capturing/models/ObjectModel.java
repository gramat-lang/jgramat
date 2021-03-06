package org.gramat.capturing.models;

public interface ObjectModel {

    Object getInstance();

    void setValue(String name, Object value);

    void addValue(String name, Object value);

    void addValue(Object value);
}
