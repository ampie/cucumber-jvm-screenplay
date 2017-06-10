package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.actors.Performance;

import java.util.Map;

public interface Scene {
    String PARENT_SCENE = "parentScene";

    String getSceneIdentifier();

    Performance getPerformance();

    int getLevel();

    Map<String, ? extends ActorOnStage> getActorsOnStage();

    ActorOnStage callActorToStage(Actor actor);

    void dismissActorFromStage(Actor actor);

    String getName();

    <T> T recall(String variableName);

    void remember(String variableName, Object value);

    ActorOnStage shineSpotlightOn(Actor actor);

    ActorOnStage theActorInTheSpotlight();

}
