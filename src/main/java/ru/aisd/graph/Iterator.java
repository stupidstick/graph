package ru.aisd.graph;

public interface Iterator<T> {

    void next();

    boolean hasSet();

    T get();
}
