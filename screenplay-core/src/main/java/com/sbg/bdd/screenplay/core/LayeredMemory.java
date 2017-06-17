package com.sbg.bdd.screenplay.core;

public interface LayeredMemory extends Memory{
    void remember(Object value);

    void forget(String name);

    <T> T recall(Class<T> clzz);

    <T> T recallImmediately(String name);
}
