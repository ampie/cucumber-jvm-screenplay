package com.sbg.bdd.screenplay.wiremock.listeners;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.internal.BaseActorOnStage;
import com.sbg.bdd.screenplay.scoped.FunctionalScope;
import com.sbg.bdd.screenplay.scoped.PayloadConsumingListener;
import com.sbg.bdd.screenplay.scoped.ScenarioScope;
import com.sbg.bdd.screenplay.scoped.UserTrackingScope;
import com.sbg.bdd.screenplay.wiremock.CorrelationPath;
import com.sbg.bdd.screenplay.wiremock.WireMockMemories;
import com.sbg.bdd.screenplay.wiremock.WireMockScreenplayContext;
import com.sbg.bdd.wiremock.scoped.admin.model.CorrelationState;
import com.sbg.bdd.wiremock.scoped.admin.model.GlobalCorrelationState;
import com.sbg.bdd.wiremock.scoped.admin.model.ServiceInvocationCount;
import com.sbg.bdd.wiremock.scoped.client.ScopedWireMockClient;
import com.sbg.bdd.wiremock.scoped.common.ParentPath;
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory;
import com.sbg.bdd.wiremock.scoped.integration.RuntimeCorrelationState;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static com.sbg.bdd.screenplay.core.actors.OnStage.theCurrentScene;

public class WireMockSync implements PayloadConsumingListener {

    @Override
    public void scopeStarted(Scene scene) {
        registerScope((UserTrackingScope) scene, Collections.<String, Object>emptyMap());
    }

    @Override
    public void scopeCompleted(Scene scene, Map<String, Object> payload) {
        String knownScopePath = CorrelationPath.of(scene);
        if (scene.getLevel() == 0) {
            getWireMockFrom(scene).stopGlobalScope(scene.<GlobalCorrelationState>recall(WireMockScreenplayContext.CORRELATION_STATE));
        } else {
            getWireMockFrom(scene).stopNestedScope(knownScopePath, payload);
        }
    }

    @Override
    public void featureStarted(FunctionalScope scene, Map<String, Object> map) {
        registerScope(scene, map);
    }

    private void registerScope(UserTrackingScope scene, Map<String, Object> map) {
        ScopedWireMockClient wireMock = getWireMockFrom(scene);
        if (scene.getLevel() == 0) {
            WireMockMemories recall = WireMockMemories.recallFrom(scene);
            String name = scene.getPerformance().getName();
            URL wireMockPublicUrl = recall.thePublicAddressOfWireMock();
            URL urlOfServiceUnderTest = recall.theBaseUrlOfTheServiceUnderTest();
            String integrationScope = recall.theIntegrationScope();
            GlobalCorrelationState inputState = new GlobalCorrelationState(name, wireMockPublicUrl, urlOfServiceUnderTest, integrationScope);
            inputState.setGlobalJournaMode(recall.theJournalModeToUse());
            GlobalCorrelationState correlationState = wireMock.startNewGlobalScope(inputState);
            scene.remember(WireMockScreenplayContext.CORRELATION_STATE, correlationState);
        } else {
            String parentScopePath = CorrelationPath.of(scene.getContainingScope());
            CorrelationState correlationState = wireMock.startNestedScope(parentScopePath, scene.getId(), map);
            scene.remember(WireMockScreenplayContext.CORRELATION_STATE, correlationState);
        }
    }

    private ScopedWireMockClient getWireMockFrom(Scene scene) {
        return scene.getPerformance().recall(WireMockScreenplayContext.SCOPED_WIRE_MOCK_CLIENT);
    }

    @Override
    public void scenarioPhaseEntered(ScenarioScope scene, Map<String, Object> payload) {
        registerScope(scene, payload);
    }

    @Override
    public void stepStarted(StepEvent event, Map<String, Object> payload) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).startStep(CorrelationPath.of(theCurrentScene), event.getStepPath(), payload);
        ((CorrelationState) theCurrentScene.recall(WireMockScreenplayContext.CORRELATION_STATE)).setCurrentStep(event.getStepPath());
    }

    @Override
    public void stepCompleted(StepEvent event, Map<String, Object> payload) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).stopStep(CorrelationPath.of(theCurrentScene), event.getStepPath(), payload);
        String stepPath = ParentPath.of(event.getStepPath());
        ((CorrelationState) theCurrentScene.recall(WireMockScreenplayContext.CORRELATION_STATE)).setCurrentStep(stepPath);
    }



    @Override
    public void beforePersonaLoaded(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload) {
        getWireMockFrom(theCurrentScene).startStep(CorrelationPath.of(theCurrentScene), "loadPersona", payload);
    }
    @Override
    public void afterPersonaLoaded(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload) {
        getWireMockFrom(theCurrentScene).stopStep(CorrelationPath.of(theCurrentScene), "loadPersona", payload);
    }

    @Override
    public void afterIntoSpotlight(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload) {
        getWireMockFrom(theCurrentScene).stopStep(CorrelationPath.of(theCurrentScene), "shineSpotlightOn", payload);
        if (!BaseActorOnStage.isEverybody(actorOnStage)) {
            CorrelationState state = actorOnStage.recall(WireMockScreenplayContext.CORRELATION_STATE);
            RuntimeCorrelationState currentCorrelationState = DependencyInjectionAdaptorFactory.getAdaptor().getCurrentCorrelationState();
            currentCorrelationState.clear();
            currentCorrelationState.set(state.getCorrelationPath(), 1, Boolean.TRUE.equals(actorOnStage.recall(WireMockScreenplayContext.PROXY_UNMAPPED_ENDPOINTS)));
            for (ServiceInvocationCount entry : state.getServiceInvocationCounts()) {
                currentCorrelationState.initSequenceNumberFor(new com.sbg.bdd.wiremock.scoped.integration.ServiceInvocationCount(entry.toString()));
            }
        }
    }


    @Override
    public void beforeIntoSpotlight(Scene theCurrentScene, ActorOnStage actorOnStage, Map<String, Object> payload) {
        getWireMockFrom(theCurrentScene).startStep(CorrelationPath.of(theCurrentScene), "shineSpotlightOn", payload);
    }
    @Override
    public  void beforeEnterStage(ActorOnStage userInScope, Map<String, Object> payload) {
        String parentScopePath = CorrelationPath.of(userInScope.getScene());
        CorrelationState state = getWireMockFrom(userInScope.getScene()).startUserScope(parentScopePath, userInScope.getId(), payload);
        userInScope.remember(WireMockScreenplayContext.CORRELATION_STATE, state);
    }

}
