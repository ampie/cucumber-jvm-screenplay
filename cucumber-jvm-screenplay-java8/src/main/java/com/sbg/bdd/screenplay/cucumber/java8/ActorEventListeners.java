package com.sbg.bdd.screenplay.cucumber.java8;

import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement;
import com.sbg.bdd.screenplay.core.events.ActorEvent;
import com.sbg.bdd.screenplay.core.events.ActorEventCallback;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;

import java.lang.reflect.Method;

public interface ActorEventListeners {
    //TODO support other method contracts too
    interface ReceivesActorEvent {
        void receive(ActorEvent event) throws Throwable;
        
        default Method getMethod() {
            return METHOD;
        }
        
        Method METHOD = resolveReceive(ReceivesActorEvent.class, ActorEvent.class);
    }
    
    
    default void onActor(ActorInvolvement type, String namePattern, int actorLevel, ReceivesActorEvent receptor) {
        register(type, new ActorEventCallback(receptor, receptor.getMethod(), namePattern, actorLevel));
    }

    default void register(ActorInvolvement type, ActorEventCallback actorEventCallback) {
        ScreenPlayEventBus.registerCallback(type, actorEventCallback);
    }
    
    default void onActor(ActorInvolvement type, String namePattern, ReceivesActorEvent receptor) {
        register(type, new ActorEventCallback(receptor, receptor.getMethod(), namePattern, -1));
    }
    
    default void onActor(ActorInvolvement type, ReceivesActorEvent receptor) {
        register(type, new ActorEventCallback(receptor, receptor.getMethod(), ".*", -1));
    }
    
    default void onActor(ActorInvolvement type, int actorLevel, ReceivesActorEvent receptor) {
        register(type, new ActorEventCallback(receptor, receptor.getMethod(), ".*", actorLevel));
    }

    
    static Method resolveReceive(Class<?> receivesActorEventClass, Class<?>... actorEventClass) {
        try {
            return receivesActorEventClass.getMethod("receive", actorEventClass);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    
}
