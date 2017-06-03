package com.sbg.bdd.screenplay.scoped;


import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.actors.Cast;
import com.sbg.bdd.screenplay.core.actors.CastingDirector;
import com.sbg.bdd.screenplay.core.actors.Performance;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;

import java.io.File;

public class GlobalScope extends UserTrackingScope implements Performance {
    private final ScreenPlayEventBus scopeEventBus;
    private Cast cast;


    public GlobalScope(String name,  CastingDirector castingDirector, ScreenPlayEventBus scopeEventBus) {
        super(null, name);
        this.cast = new Cast(castingDirector);
        this.scopeEventBus = scopeEventBus;
    }

    @Override
    public String getSceneIdentifier() {
        return "";
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

    @Override
    protected void completeWithoutEvents() {
        super.completeWithoutEvents();
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
