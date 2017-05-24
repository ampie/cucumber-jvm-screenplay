package cucumber.scoping;


import cucumber.screenplay.Scene;
import cucumber.screenplay.events.SceneEvent;
import cucumber.screenplay.events.ActorEvent;
import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.actors.Cast;
import cucumber.screenplay.actors.CastingDirector;
import cucumber.screenplay.actors.Performance;
import cucumber.screenplay.events.ScreenPlayEventBus;
import cucumber.screenplay.persona.CharacterType;
import cucumber.screenplay.persona.Persona;

import java.nio.file.Path;
import java.util.Map;

public class GlobalScope extends UserTrackingScope implements Performance {
    private final ScreenPlayEventBus scopeEventBus;
    private final Path resourceRoot;
    private Cast cast;


    public GlobalScope(String name, Path resourceRoot, CastingDirector castingDirector, ScreenPlayEventBus scopeEventBus) {
        super(null, name);
        this.cast = new Cast(castingDirector);
        this.scopeEventBus = scopeEventBus;
        this.resourceRoot = resourceRoot;
    }

    public Path getResourceRoot() {
        return resourceRoot;
    }

    @Override
    public Scene currentScene() {
        return getInnerMostActive(Scene.class);
    }

    @Override
    public ScreenPlayEventBus getEventBus() {
        return scopeEventBus;
    }


    @Override
    public Scene raiseTheCurtain(String sceneName) {
        return getInnerMostActive(Scene.class);
    }

    public void broadcast(SceneEvent event) {
        scopeEventBus.broadcast(event);
    }

    public void drawTheCurtain() {
        UserTrackingScope active = getInnerMostActive(UserTrackingScope.class);
        active.getContainingScope().completeNestedScope(active.getName());
    }

    @Override
    public int getLevel() {
        return 0;
    }


    public FunctionalScope startFunctionalScope(String name) {
        return setupChild(new FunctionalScope(this, name));
    }

    @Override
    public String getScopePath() {
        return getId();
    }

    @Override
    public GlobalScope getGlobalScope() {
        return this;
    }

    public Cast getCast() {
        return cast;
    }

}
