package com.sbg.bdd.screenplay.core.events;

import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.annotations.SceneEventType;

import java.util.EventObject;

public class SceneEvent extends EventObject {
    private final SceneEventType sceneEventType;
    private String phase;

    public SceneEvent(Scene source, SceneEventType sceneEventType,String phase){
        this(source,sceneEventType);
        this.phase = phase;
    }
    public SceneEvent(Scene source, SceneEventType sceneEventType) {
        super(source);
        this.sceneEventType = sceneEventType;
    }

    public String getPhase() {
        return phase;
    }

    public SceneEventType getSceneEventType() {
        return sceneEventType;
    }

    public Scene getScene() {
        return (Scene) getSource();
    }
}
