package com.sbg.bdd.screenplay.cucumber.java8;

import com.sbg.bdd.screenplay.core.annotations.SceneEventType;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.core.events.SceneEventCallback;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.lang.reflect.Method;

public interface SceneEventListeners {
    //TODO support other method contracts too
    interface ReceivesSceneEvent {
        void receive(SceneEvent event) throws Throwable;
        
        default Method getMethod() {
            return METHOD;
        }
        
        Method METHOD = resolveReceive(ReceivesSceneEvent.class, SceneEvent.class);
    }
    
    
    default void onScene(SceneEventType type, String namePattern, int sceneLevel, ReceivesSceneEvent receptor) {
        register(type, new SceneEventCallback(receptor, receptor.getMethod(), namePattern, sceneLevel));
    }

    default void register(SceneEventType type, SceneEventCallback sceneEventCallback) {
        ScreenPlayEventBus.registerCallback(type, sceneEventCallback);
    }
    
    default void onScene(SceneEventType type, String namePattern, ReceivesSceneEvent receptor) {
        register(type, new SceneEventCallback(receptor, receptor.getMethod(), namePattern, -1));
    }
    
    default void onScene(SceneEventType type, ReceivesSceneEvent receptor) {
        register(type, new SceneEventCallback(receptor, receptor.getMethod(), ".*", -1));
    }
    
    default void onScene(SceneEventType type, int sceneLevel, ReceivesSceneEvent receptor) {
        register(type, new SceneEventCallback(receptor, receptor.getMethod(), ".*", sceneLevel));
    }

    
    static Method resolveReceive(Class<?> receivesSceneEventClass, Class<?>... sceneEventClass) {
        try {
            return receivesSceneEventClass.getMethod("receive", sceneEventClass);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    
}
