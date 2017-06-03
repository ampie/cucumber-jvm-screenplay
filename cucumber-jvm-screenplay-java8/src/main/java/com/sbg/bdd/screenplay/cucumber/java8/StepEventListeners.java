package com.sbg.bdd.screenplay.cucumber.java8;

import com.sbg.bdd.screenplay.core.annotations.StepEventType;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.events.StepEventCallback;

import java.lang.reflect.Method;

public interface StepEventListeners {
    //TODO support other method contracts too
    interface ReceivesStepEvent {
        void receive(StepEvent event);
        
        default Method getMethod() {
            return METHOD;
        }
        
        Method METHOD = resolveReceive(ReceivesStepEvent.class, StepEvent.class);
    }
    
    default void onStep(StepEventType type, ReceivesStepEvent receptor) {
        onStep(type, ".*", receptor);
    }

    default void onStep(StepEventType type, String namePattern, ReceivesStepEvent receptor) {
        onStep(type, namePattern, -1, receptor);
    }

    default void onStep(StepEventType type, int stepLevel, ReceivesStepEvent receptor) {
        onStep(type, ".*", stepLevel, receptor);
    }

    default void onAnyStepEvent(ReceivesStepEvent receptor) {
        onAnyStepEvent(".*", receptor);
    }

    default void onAnyStepEvent(String namePattern, ReceivesStepEvent receptor) {
        onAnyStepEvent(namePattern, -1, receptor);
    }
    default void onAnyStepEvent(int stepLevel, ReceivesStepEvent receptor) {
        onAnyStepEvent(".*", stepLevel, receptor);
    }

    default void onAnyStepEvent(String namePattern, int stepLevel, ReceivesStepEvent receptor) {
        for (StepEventType type : StepEventType.values()) {
            onStep(type, namePattern, stepLevel, receptor);
        }
    }

    default void onStep(StepEventType type, String namePattern, int stepLevel, ReceivesStepEvent receptor) {
        ScreenPlayEventBus.registerCallback(type, new StepEventCallback(receptor, receptor.getMethod(), namePattern, stepLevel));
    }

    static Method resolveReceive(Class<?> receivesStepEventClass, Class<?>... stepEventClass) {
        try {
            return receivesStepEventClass.getMethod("receive", stepEventClass);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    
}
