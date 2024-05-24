package ru.stupidstick.graph;

public interface Iterator<T> {

    void next();

    boolean hasSet();

    T get();
}
