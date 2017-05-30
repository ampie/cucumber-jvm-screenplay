package com.sbg.bdd.screenplay.core.internal;

/**
 * Created by ampie on 2017/05/22.
 */
public interface InstanceGetter {
    <T> T getInstance(Class<T> type);
}
