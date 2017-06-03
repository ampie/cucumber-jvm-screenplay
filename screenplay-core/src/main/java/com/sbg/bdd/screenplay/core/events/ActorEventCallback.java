package com.sbg.bdd.screenplay.core.events;


import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.annotations.ActorListener;

import java.lang.reflect.Method;

import static com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus.mostSpecific;


public class ActorEventCallback extends ScreenPlayEventCallback {
    private final Class<?> type;

    public ActorEventCallback(Object target, Method method, ActorListener b) {
        this(target, method, b.scopeType(), b.namePattern(), b.sceneLevel());
    }

    public ActorEventCallback(Object target, Method method, String namePattern, int level) {
        this(target, method, ActorOnStage.class, namePattern, level);
    }

    public ActorEventCallback(Object target, Method method, Class<? extends ActorOnStage> type, String namePattern, int level) {
        super(target, method, namePattern, level);
        this.type = mostSpecific(method, type);

    }

    public boolean isMatch(ActorEvent event) {
        return type.isInstance(event.getActorOnStage()) && levelsMatch(event.getActorOnStage().getScene().getLevel()) &&
                namesMatch(event.getActorOnStage().getActor().getName());
    }


}
