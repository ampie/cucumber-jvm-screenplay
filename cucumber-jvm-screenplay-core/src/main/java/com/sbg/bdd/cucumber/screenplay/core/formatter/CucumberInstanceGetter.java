package com.sbg.bdd.cucumber.screenplay.core.formatter;

import com.sbg.bdd.screenplay.core.internal.InstanceGetter;
import cucumber.api.java.ObjectFactory;

public class CucumberInstanceGetter implements InstanceGetter {
    private ObjectFactory objectFactory;

    public CucumberInstanceGetter(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return objectFactory.getInstance(type);
    }
}
