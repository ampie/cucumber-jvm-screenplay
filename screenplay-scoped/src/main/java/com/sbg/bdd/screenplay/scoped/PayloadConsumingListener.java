package com.sbg.bdd.screenplay.scoped;

import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.events.StepEvent;

import java.util.Map;

/**
 * A useful interface  that provides callbacks for the most commonly used Scoped Screenplay events.
 * Each of these callbacks also take a Map<String,Object> that typically contains a payload that can be forwarded
 * either to a reporting facility or to another scope maintaining framework
 */
public interface PayloadConsumingListener {

    void scopeStarted(Scene scene);

    void featureStarted(FunctionalScope scene, Map<String, Object> map);

    void scenarioPhaseEntered(ScenarioScope scene, Map<String, Object> payload);

    void stepStarted(StepEvent event, Map<String, Object> payload);

    void stepCompleted(StepEvent event, Map<String, Object> payload);

    void scopeCompleted(Scene scene, Map<String, Object> payload);

    void beforePersonaLoaded(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload);

    void afterPersonaLoaded(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload);

    void beforeIntoSpotlight(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload);

    void afterIntoSpotlight(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload);

    void beforeEnterStage(ActorOnStage actorOnStage, Map<String, Object> payload);
}
