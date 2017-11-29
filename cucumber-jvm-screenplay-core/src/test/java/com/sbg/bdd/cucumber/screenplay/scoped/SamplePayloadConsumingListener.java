package com.sbg.bdd.cucumber.screenplay.scoped;

import com.sbg.bdd.cucumber.screenplay.core.formatter.MapParser;
import com.sbg.bdd.cucumber.screenplay.core.formatter.ScreenPlayFormatter;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.scoped.FunctionalScope;
import com.sbg.bdd.screenplay.scoped.PayloadConsumingListener;
import com.sbg.bdd.screenplay.scoped.ScenarioScope;

import java.io.IOException;
import java.util.Map;

public class SamplePayloadConsumingListener implements PayloadConsumingListener{


    static MapParser formatter;

    public static void setOutput(ScreenPlayFormatter formatter) throws IOException {
        SamplePayloadConsumingListener.formatter = new MapParser(formatter, formatter);
    }

    @Override
    public void scenarioPhaseEntered(ScenarioScope scene, Map<String, Object> payload) {
        if (formatter != null) {
            formatter.replayFeatureElement(payload);
        }

    }

    @Override
    public void scopeStarted(Scene scene) {

    }

    @Override
    public void featureStarted(FunctionalScope scene, Map<String, Object> map) {
        if (formatter != null) {
            formatter.replayFeature(map);
        }
    }

    @Override
    public void stepStarted(StepEvent event, Map<String, Object> payload) {
        if (formatter != null) {
            if ("childStepAndMatch".equals(payload.get("method"))) {
                formatter.replayChildStepAndMatch(payload);
            } else {
                formatter.replayStepAndMatch(payload);
            }
        }
    }

    @Override
    public void stepCompleted(StepEvent event, Map<String, Object> payload) {
        if(formatter!=null){
            if ("childResult".equals(payload.get("method"))) {
                formatter.replayChildResult(payload);
            } else {
                formatter.replayResult(payload);
            }
        }
    }

    @Override
    public void scopeCompleted(Scene scene, Map<String, Object> payload) {

    }

    @Override
    public void beforePersonaLoaded(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload) {

    }

    @Override
    public void afterPersonaLoaded(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload) {

    }

    @Override
    public void beforeIntoSpotlight(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload) {

    }

    @Override
    public void afterIntoSpotlight(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload) {

    }

    @Override
    public void beforeEnterStage(ActorOnStage actorOnStage, Map<String, Object> payload) {

    }
}
