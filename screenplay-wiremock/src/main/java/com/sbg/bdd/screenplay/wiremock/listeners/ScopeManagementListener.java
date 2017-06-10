package com.sbg.bdd.screenplay.wiremock.listeners;


import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.annotations.*;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.internal.BaseActorOnStage;
import com.sbg.bdd.screenplay.scoped.listeners.ScreenplayLifecycleSync;
import com.sbg.bdd.screenplay.wiremock.CorrelationPath;
import com.sbg.bdd.screenplay.wiremock.WireMockScreenplayContext;
import com.sbg.bdd.wiremock.scoped.admin.model.CorrelationState;
import com.sbg.bdd.wiremock.scoped.common.ParentPath;
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory;
import com.sbg.bdd.wiremock.scoped.integration.WireMockCorrelationState;
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient;

import java.util.Map;

import static com.sbg.bdd.screenplay.core.actors.OnStage.theCurrentScene;
import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;

@Within(ScreenplayLifecycleSync.class)
public class ScopeManagementListener {

    @SceneListener(scopePhases = SceneEventType.BEFORE_START)
    public void registerScope(Scene scene) {
        RecordingWireMockClient wireMock = getWireMockFrom(scene);
        String scopePath = CorrelationPath.of(scene);
        CorrelationState correlationState = wireMock.joinCorrelatedScope(scopePath);
        scene.remember(WireMockScreenplayContext.CORRELATION_STATE, correlationState);
        syncCorrelationState(scene, correlationState);
    }

    private void syncCorrelationState(Scene scene, CorrelationState correlationState) {
        WireMockCorrelationState currentCorrelationState = DependencyInjectionAdaptorFactory.getAdaptor().getCurrentCorrelationState();
        currentCorrelationState.set(correlationState.getCorrelationPath(), Boolean.TRUE.equals(scene.recall(WireMockScreenplayContext.PROXY_UNMAPPED_ENDPOINGS)));
        for (Map.Entry<String, Integer> entry : correlationState.getServiceInvocationCounts().entrySet()) {
            currentCorrelationState.initSequenceNumberFor(entry.getKey(), entry.getValue());
        }
    }

    @SceneListener(scopePhases = SceneEventType.AFTER_COMPLETE)
    public void unregisterScope(Scene scene) {
        String knownScopePath = CorrelationPath.of(scene);
        getWireMockFrom(scene).stopCorrelatedScope(knownScopePath);
        if (scene.recall(Scene.PARENT_SCENE) != null) {
            Scene parentScene = scene.recall(Scene.PARENT_SCENE);
            CorrelationState correlationState = parentScene.recall(WireMockScreenplayContext.CORRELATION_STATE);
            syncCorrelationState(scene, correlationState);
        }
    }

    @StepListener(eventTypes = StepEventType.STARTED)
    public void registerStep(StepEvent event) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).startStep(CorrelationPath.of(theCurrentScene), event.getStepPath());
        ((CorrelationState) theCurrentScene.recall(WireMockScreenplayContext.CORRELATION_STATE)).setCurrentStep(event.getStepPath());
    }

    @StepListener(eventTypes = {PENDING, SKIPPED, ASSERTION_FAILED, SUCCESSFUL, FAILED})
    public void unregisterStep(StepEvent stepEvent) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).stopStep(CorrelationPath.of(theCurrentScene), stepEvent.getStepPath());
        String stepPath = ParentPath.of(stepEvent.getStepPath());
        ((CorrelationState) theCurrentScene.recall(WireMockScreenplayContext.CORRELATION_STATE)).setCurrentStep(stepPath);
    }

    @ActorListener(involvement = ActorInvolvement.BEFORE_ENTER_STAGE)
    public void registerScope(ActorOnStage userInScope) {
        if (!BaseActorOnStage.isEverybody(userInScope)) {
            String scopePath = CorrelationPath.of(userInScope);
            CorrelationState state = getWireMockFrom(userInScope.getScene()).joinCorrelatedScope(scopePath);
            syncCorrelationState(userInScope.getScene(), state);
            userInScope.remember(WireMockScreenplayContext.CORRELATION_STATE, state);
        }
    }

    private RecordingWireMockClient getWireMockFrom(Scene scene) {
        return scene.getPerformance().recall(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT);
    }
}
