package com.sbg.bdd.screenplay.core.internal;

import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.resource.file.RootDirectoryResource;
import com.sbg.bdd.screenplay.core.Memory;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.actors.Cast;
import com.sbg.bdd.screenplay.core.actors.Performance;
import com.sbg.bdd.screenplay.core.annotations.SceneEventType;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.screenplay.core.persona.properties.PropertiesPersonaClient;

import java.io.File;

public class BasePerformance implements Performance {
    private final String name
            ;
    protected ScreenPlayEventBus eventBus;
    protected BaseCastingDirector castingDirector;
    protected Cast cast;
    private Scene scene;
    protected Memory memory = new SimpleMemory();

    //For tests mainly
    public BasePerformance(String name, ResourceContainer inputResourceRoot) {
        this(name,inputResourceRoot, new PropertiesPersonaClient(), new SimpleInstanceGetter());
    }

    public BasePerformance(String name, ResourceContainer inputResourceRoot, PersonaClient<?> personaClient, InstanceGetter instanceGetter) {
        eventBus = new ScreenPlayEventBus(instanceGetter);
        castingDirector = new BaseCastingDirector(eventBus, personaClient, inputResourceRoot);
        cast = new Cast(castingDirector);
        memory.remember("inputResourceRoot",inputResourceRoot);
        this.name=name;
    }

    @Override
    public String getName() {
        return name;
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
    public Scene currentScene() {
        return scene;
    }

    @Override
    public ScreenPlayEventBus getEventBus() {
        return eventBus;
    }

    public void remember(String name, Object value) {
        memory.remember(name, value);
    }
}
