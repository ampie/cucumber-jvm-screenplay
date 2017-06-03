package com.sbg.bdd.screenplay.core.events;

import com.sbg.bdd.screenplay.core.annotations.StepListener;

import java.lang.reflect.Method;


public class StepEventCallback extends ScreenPlayEventCallback {
    public StepEventCallback(Object target, Method method, StepListener stepListener) {
        this(target, method,stepListener.namePattern(),stepListener.stepLevel());
    }
    public StepEventCallback(Object target, Method method, String namePattern, int stepLevel) {
        super(target, method,namePattern,stepLevel);
    }
}
