package com.sbg.bdd.screenplay.core.events;


import com.sbg.bdd.screenplay.core.annotations.SceneEventType;
import com.sbg.bdd.screenplay.core.annotations.SceneListener;

import java.lang.reflect.Method;

import static com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus.mostSpecific;


public class SceneEventCallback extends ScreenPlayEventCallback {
    private SceneEventType sceneEventType;
    private int level;
    private Class<?> type;

    public SceneEventCallback(Object target, Method method, SceneListener b, SceneEventType phase) {
        super(target, method, b.namePattern());
        sceneEventType = phase;
        this.level = b.level();
        type = mostSpecific(method, b.sceneType());
    }


    public boolean isMatch(SceneEvent event) {
        return type.isInstance(event.getScene()) && this.sceneEventType == event.getSceneEventType() && levelsMatch(event.getScene().getLevel()) &&
                namesMatch(event.getScene().getName());
    }

    private boolean levelsMatch(int levelToMatch) {
        return level == -1 || levelToMatch == level;
    }


}
