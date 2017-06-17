package com.sbg.bdd.screenplay.core.internal;


import com.sbg.bdd.screenplay.core.Memory;

import java.util.HashMap;
import java.util.Map;

public class SimpleMemory implements Memory {
    private final Map<String, Object> storage = new HashMap<>();

    @Override
    public void remember(String name, Object value) {
        storage.put(name, value);
    }

    public void forget(String name) {
        storage.remove(name);
    }

    @Override
    public <T> T recall(String name) {
        return (T) storage.get(name);
    }


    public void clear() {
        storage.clear();
    }
}
