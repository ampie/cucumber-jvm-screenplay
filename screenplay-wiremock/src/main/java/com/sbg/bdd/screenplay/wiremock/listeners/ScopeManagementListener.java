package com.sbg.bdd.screenplay.wiremock.listeners;


import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.actors.Performance;
import com.sbg.bdd.screenplay.core.annotations.*;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.internal.BaseActorOnStage;
import com.sbg.bdd.screenplay.wiremock.CorrelationPath;
import com.sbg.bdd.screenplay.wiremock.WireMockMemories;
import com.sbg.bdd.screenplay.wiremock.WireMockScreenplayContext;
import com.sbg.bdd.wiremock.scoped.admin.model.CorrelationState;
import com.sbg.bdd.wiremock.scoped.admin.model.GlobalCorrelationState;
import com.sbg.bdd.wiremock.scoped.common.ParentPath;
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory;
import com.sbg.bdd.wiremock.scoped.integration.ServiceInvocationCount;
import com.sbg.bdd.wiremock.scoped.integration.RuntimeCorrelationState;
import com.sbg.bdd.wiremock.scoped.client.ScopedWireMockClient;

import java.net.URL;
import java.util.Collections;

import static com.sbg.bdd.screenplay.core.actors.OnStage.theCurrentScene;
import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;
@Deprecated
//Still used for tests primarily
//See com.sbg.bdd.cucumber.wiremock.listeners.ScopeManagementListener
//TODO refactor this out of existince and decouple the other ScopeManagementListener from both cucumber and from screenplay-scoped
public class ScopeManagementListener {

    @SceneListener(scopePhases = SceneEventType.BEFORE_START)
    public void registerScope(Scene scene) {
        ScopedWireMockClient wireMock = getWireMockFrom(scene);
        if (scene.getLevel() == 0) {
            GlobalCorrelationState correlationState = startGlobalScope(wireMock, scene.getPerformance());
            scene.remember(WireMockScreenplayContext.CORRELATION_STATE, correlationState);
        } else {
            GlobalCorrelationState globalCorrelationState = scene.getPerformance().recall(WireMockScreenplayContext.CORRELATION_STATE);
            if(globalCorrelationState ==null){
                globalCorrelationState = startGlobalScope(wireMock, scene.getPerformance());
                scene.getPerformance().remember(WireMockScreenplayContext.CORRELATION_STATE, globalCorrelationState);
            }
            CorrelationState correlationState = wireMock.startNestedScope(globalCorrelationState.getCorrelationPath(), scene.getSceneIdentifier(), Collections.<String, Object>emptyMap());
            scene.remember(WireMockScreenplayContext.CORRELATION_STATE, correlationState);
        }

    }

    private GlobalCorrelationState startGlobalScope(ScopedWireMockClient wireMock, Performance performance) {
        WireMockMemories recall = WireMockMemories.recallFrom(performance);
        String name = performance.getName();
        URL wireMockPublicUrl = recall.thePublicAddressOfWireMock();
        URL urlOfServiceUnderTest = recall.theBaseUrlOfTheServiceUnderTest();
        String integrationScope = recall.theIntegrationScope();
        GlobalCorrelationState inputState = new GlobalCorrelationState(name, wireMockPublicUrl, urlOfServiceUnderTest, integrationScope);
        inputState.setGlobalJournaMode(recall.theJournalModeToUse());
        return wireMock.startNewGlobalScope(inputState);
    }

    @SceneListener(scopePhases = SceneEventType.AFTER_COMPLETE)
    public void unregisterScope(Scene scene) {
        String knownScopePath = CorrelationPath.of(scene);
        getWireMockFrom(scene).stopNestedScope(knownScopePath,Collections.<String, Object>emptyMap());
    }

    @StepListener(eventTypes = StepEventType.STARTED)
    public void registerStep(StepEvent event) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).startStep(CorrelationPath.of(theCurrentScene), event.getStepPath(),Collections.<String, Object>emptyMap());
        ((CorrelationState) theCurrentScene.recall(WireMockScreenplayContext.CORRELATION_STATE)).setCurrentStep(event.getStepPath());
    }

    @StepListener(eventTypes = {PENDING, SKIPPED, ASSERTION_FAILED, SUCCESSFUL, FAILED})
    public void unregisterStep(StepEvent stepEvent) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).stopStep(CorrelationPath.of(theCurrentScene), stepEvent.getStepPath(),Collections.<String, Object>emptyMap());
        String stepPath = ParentPath.of(stepEvent.getStepPath());
        ((CorrelationState) theCurrentScene.recall(WireMockScreenplayContext.CORRELATION_STATE)).setCurrentStep(stepPath);
    }
    @ActorListener(involvement = ActorInvolvement.INTO_SPOTLIGHT)
    public void syncCorrelationState(ActorOnStage userInScope) {
        if (!BaseActorOnStage.isEverybody(userInScope)) {
            CorrelationState state = userInScope.recall(WireMockScreenplayContext.CORRELATION_STATE);
            RuntimeCorrelationState currentCorrelationState = DependencyInjectionAdaptorFactory.getAdaptor().getCurrentCorrelationState();
            currentCorrelationState.clear();
            currentCorrelationState.set(state.getCorrelationPath(), 1, Boolean.TRUE.equals(userInScope.recall(WireMockScreenplayContext.PROXY_UNMAPPED_ENDPOINTS)));
            for (com.sbg.bdd.wiremock.scoped.admin.model.ServiceInvocationCount entry : state.getServiceInvocationCounts()) {
                currentCorrelationState.initSequenceNumberFor(new ServiceInvocationCount(entry.toString()));
            }
        }
    }

    @ActorListener(involvement = ActorInvolvement.BEFORE_ENTER_STAGE)
    public void registerScope(ActorOnStage userInScope) {
        if (!BaseActorOnStage.isEverybody(userInScope)) {
            String scopePath = CorrelationPath.of(userInScope.getScene());
            CorrelationState state = getWireMockFrom(userInScope.getScene()).startUserScope(scopePath, userInScope.getId(),Collections.<String, Object>emptyMap());
            userInScope.remember(WireMockScreenplayContext.CORRELATION_STATE, state);
        }
    }

    private ScopedWireMockClient getWireMockFrom(Scene scene) {
        return scene.getPerformance().recall(WireMockScreenplayContext.SCOPED_WIRE_MOCK_CLIENT);
    }
}
