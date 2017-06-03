package com.sbg.bdd.screenplay.core.events;


import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.annotations.SceneListener;

import java.lang.reflect.Method;

import static com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus.mostSpecific;


public class SceneEventCallback extends ScreenPlayEventCallback {
    private Class<?> type;

    public SceneEventCallback(Object target, Method method, SceneListener b) {
        this(target, method, b.sceneType(), b.namePattern(), b.level());
    }

    public SceneEventCallback(Object target, Method method, Class<? extends Scene> type, String namePattern, int level) {
        super(target, method, namePattern, level);
        this.type = mostSpecific(method, type);
    }

    public SceneEventCallback(Object target, Method method, String namePattern, int level) {
        this(target, method, Scene.class, namePattern, level);
    }


    public boolean isMatch(SceneEvent event) {
        return type.isInstance(event.getScene())
                && levelsMatch(event.getScene().getLevel())
                && namesMatch(event.getScene().getName());
    }


}
