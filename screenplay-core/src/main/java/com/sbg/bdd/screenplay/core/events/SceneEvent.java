package com.sbg.bdd.screenplay.core.events;

import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.annotations.SceneEventType;

import java.util.EventObject;

public class SceneEvent extends EventObject {
    private final SceneEventType sceneEventType;

    public SceneEvent(Scene source, SceneEventType sceneEventType) {
        super(source);
        this.sceneEventType = sceneEventType;
    }

    public SceneEventType getSceneEventType() {
        return sceneEventType;
    }

    public Scene getScene() {
        return (Scene) getSource();
    }
}
