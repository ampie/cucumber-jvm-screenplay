package com.sbg.bdd.screenplay.core.internal;


public interface InstanceGetter {
    <T> T getInstance(Class<T> type);
}
