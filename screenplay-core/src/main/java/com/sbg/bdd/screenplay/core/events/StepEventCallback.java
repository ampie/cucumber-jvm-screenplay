package com.sbg.bdd.screenplay.core.events;

import com.sbg.bdd.screenplay.core.annotations.StepListener;

import java.lang.reflect.Method;


public class StepEventCallback extends ScreenPlayEventCallback {
    public StepEventCallback(Object target, Method method, StepListener stepListener) {
        super(target, method,stepListener.namePattern());
    }
}
