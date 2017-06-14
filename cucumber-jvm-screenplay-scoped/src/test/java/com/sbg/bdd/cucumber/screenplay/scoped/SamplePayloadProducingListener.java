package com.sbg.bdd.cucumber.screenplay.scoped;

import com.sbg.bdd.cucumber.common.MapParser;
import com.sbg.bdd.cucumber.common.ScreenPlayFormatter;
import com.sbg.bdd.cucumber.screenplay.scoped.plugin.CucumberPayloadProducingListener;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.events.StepEvent;

import java.io.IOException;
import java.util.Map;

public class SamplePayloadProducingListener extends CucumberPayloadProducingListener {
    private static MapParser formatter;

    public static void setOutput(ScreenPlayFormatter formatter) throws IOException {
        SamplePayloadProducingListener.formatter = new MapParser(formatter, formatter);
    }

    @Override
    protected void scenarioPhaseEntered(Scene scene, Map<String, Object> payload) {
        if (formatter != null) {
            formatter.replayFeatureElement(payload);
        }

    }

    @Override
    protected void scopeStarted(Scene scene) {

    }

    @Override
    protected void featureStarted(Scene scene, Map<String, Object> map) {
        if (formatter != null) {
            formatter.replayFeature(map);
        }
    }

    @Override
    protected void stepStarted(StepEvent event, Map<String, Object> payload) {
        if (formatter != null) {
            if ("childStepAndMatch".equals(payload.get("method"))) {
                formatter.replayChildStepAndMatch(payload);
            } else {
                formatter.replayStepAndMatch(payload);
            }
        }
    }

    @Override
    protected void stepCompleted(StepEvent event, Map<String, Object> payload) {
        if(formatter!=null){
            if ("childResult".equals(payload.get("method"))) {
                formatter.replayChildResult(payload);
            } else {
                formatter.replayResult(payload);
            }
        }
    }
}
