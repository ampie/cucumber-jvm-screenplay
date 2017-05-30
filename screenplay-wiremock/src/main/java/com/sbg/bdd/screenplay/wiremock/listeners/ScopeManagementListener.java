package com.sbg.bdd.screenplay.wiremock.listeners;


import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.annotations.*;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.internal.BaseActorOnStage;
import com.sbg.bdd.screenplay.wiremock.CorrelationPath;
import com.sbg.bdd.wiremock.scoped.admin.model.CorrelationState;
import com.sbg.bdd.wiremock.scoped.common.ParentPath;
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient;

import static com.sbg.bdd.screenplay.core.actors.OnStage.theCurrentScene;
import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;

public class ScopeManagementListener {
    @SceneListener(scopePhases = SceneEventType.BEFORE_START)
    public void registerScope(Scene scene) {
        String scopePath = CorrelationPath.of(scene);
        CorrelationState correlationState = getWireMockFrom(scene).joinCorrelatedScope(scopePath);
        scene.remember("correlationState", correlationState);
    }

    @SceneListener(scopePhases = SceneEventType.AFTER_COMPLETE)
    public void unregisterScope(Scene scene) {
        String knownScopePath = CorrelationPath.of(scene);
        getWireMockFrom(scene).stopCorrelatedScope(knownScopePath);
    }

    @StepListener(eventTypes = StepEventType.STEP_STARTED)
    public void registerStep(StepEvent event) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).startStep(CorrelationPath.of(theCurrentScene), event.getStepPath());
        ((CorrelationState)theCurrentScene.recall("correlationState")).setCurrentStep(event.getStepPath());
    }

    @StepListener(eventTypes = {STEP_PENDING, STEP_SKIPPED, STEP_ASSERTION_FAILED, STEP_SUCCESSFUL, STEP_FAILED})
    public void unregisterStep(StepEvent stepEvent) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).stopStep(CorrelationPath.of(theCurrentScene), stepEvent.getStepPath());
        String stepPath = ParentPath.of(stepEvent.getStepPath());
        ((CorrelationState)theCurrentScene.recall("correlationState")).setCurrentStep(stepPath);

    }


    @ActorListener(involvement = ActorInvolvement.BEFORE_ENTER_STAGE)
    public void registerScope(ActorOnStage userInScope) {
        if (!BaseActorOnStage.isEverybody(userInScope)) {
            String scopePath = CorrelationPath.of(userInScope);
            CorrelationState state = getWireMockFrom(userInScope.getScene()).joinCorrelatedScope(scopePath);
            userInScope.remember("correlationState", state);
        }
    }
    private RecordingWireMockClient getWireMockFrom(Scene scene) {
        return scene.getPerformance().recall("recordingWireMockClient");
    }



}
