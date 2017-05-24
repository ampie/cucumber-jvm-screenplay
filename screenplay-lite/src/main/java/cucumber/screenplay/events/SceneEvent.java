package cucumber.screenplay.events;

import cucumber.screenplay.Scene;
import cucumber.screenplay.annotations.SceneEventType;

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
