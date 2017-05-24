package cucumber.screenplay.internal;

import cucumber.screenplay.Memory;
import cucumber.screenplay.Scene;
import cucumber.screenplay.actors.Cast;
import cucumber.screenplay.actors.CastingDirector;
import cucumber.screenplay.actors.Performance;
import cucumber.screenplay.annotations.SceneEventType;
import cucumber.screenplay.events.SceneEvent;
import cucumber.screenplay.events.ScreenPlayEventBus;
import cucumber.screenplay.persona.properties.PropertiesPersonaClient;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BasePerformance implements Performance {
    private final Path resourceRoot;
    protected ScreenPlayEventBus eventBus;
    protected BaseCastingDirector castingDirector;
    protected Cast cast;
    private Scene scene;
    private Memory memory = new SimpleMemory();

    public BasePerformance() {
        //For tests mainly
        resourceRoot = Paths.get("src/test/resources");
        eventBus = new ScreenPlayEventBus(new SimpleInstanceGetter());
        castingDirector = new BaseCastingDirector(eventBus, new PropertiesPersonaClient(), resourceRoot);
        cast = new Cast(castingDirector);
    }

    public BasePerformance(Path resourceRoot, CastingDirector castingDirector, ScreenPlayEventBus eventBus) {
        this.cast = new Cast(castingDirector);
        this.eventBus = eventBus;
        this.resourceRoot = resourceRoot;
    }


    @Override
    public Cast getCast() {
        return cast;
    }


    @Override
    public void drawTheCurtain() {
        Scene tempScene = this.scene;
        getEventBus().broadcast(new SceneEvent(tempScene, SceneEventType.BEFORE_COMPLETE));
        this.scene = null;
        getEventBus().broadcast(new SceneEvent(tempScene, SceneEventType.AFTER_COMPLETE));

    }

    @Override
    public Scene raiseTheCurtain(String sceneName) {
        Scene tempScene = new BaseScene(this, sceneName);
        getEventBus().broadcast(new SceneEvent(tempScene, SceneEventType.BEFORE_START));
        this.scene = tempScene;
        getEventBus().broadcast(new SceneEvent(tempScene, SceneEventType.AFTER_START));
        return this.scene;
    }

    @Override
    public <T> T recall(String variableName) {
        return memory.recall(variableName);
    }

    @Override
    public Path getResourceRoot() {
        return resourceRoot;
    }

    @Override
    public Scene currentScene() {
        return scene;
    }

    @Override
    public ScreenPlayEventBus getEventBus() {
        return eventBus;
    }

}
