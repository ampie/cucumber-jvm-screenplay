package com.sbg.bdd.screenplay.core;


public interface Memory {
    void remember(String name, Object value);

    <T> T recall(String name);

}
