package com.sbg.bdd.cucumber.wiremock.listeners;

import com.sbg.bdd.cucumber.screenplay.scoped.plugin.CucumberPayloadProducingListener;
import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement;
import com.sbg.bdd.screenplay.core.annotations.ActorListener;
import com.sbg.bdd.screenplay.core.annotations.SceneEventType;
import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.core.events.ActorEvent;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.internal.BaseActorOnStage;
import com.sbg.bdd.screenplay.scoped.FunctionalScope;
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
import com.sbg.bdd.wiremock.scoped.integration.WireMockCorrelationState;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sbg.bdd.screenplay.core.actors.OnStage.theCurrentScene;

public class ScopeManagementListener extends CucumberPayloadProducingListener {
    public static final String UPDATE_JIRA = "serenity.jira.integration.enabled";
    public static final String[] COMPLETION_STATE = {UPDATE_JIRA};

    @SceneListener(scopePhases = SceneEventType.AFTER_COMPLETE)
    public void unregisterScope(Scene scene) {
        String knownScopePath = CorrelationPath.of(scene);
        Map<String, Object> payload = new HashMap<>();
        for (String s : COMPLETION_STATE) {
            Object value = scene.recall(s);
            if (value != null) {
                payload.put(s, value);
            }
        }
        if (scene.getLevel() == 0) {
            getWireMockFrom(scene).stopGlobalScope(scene.<GlobalCorrelationState>recall(WireMockScreenplayContext.CORRELATION_STATE));
        } else {
            getWireMockFrom(scene).stopNestedScope(knownScopePath, payload);
        }
    }

    @Override
    protected void scopeStarted(Scene scene) {
        registerScope((UserTrackingScope) scene, Collections.<String, Object>emptyMap());
    }

    @Override
    protected void featureStarted(FunctionalScope scene, Map<String, Object> map) {
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

    @Override
    protected void scenarioPhaseEntered(ScenarioScope scene, Map<String, Object> payload) {
        registerScope(scene, payload);
    }

    @Override
    protected void stepStarted(StepEvent event, Map<String, Object> payload) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).startStep(CorrelationPath.of(theCurrentScene), event.getStepPath(), payload);
        ((CorrelationState) theCurrentScene.recall(WireMockScreenplayContext.CORRELATION_STATE)).setCurrentStep(event.getStepPath());
    }

    @Override
    protected void stepCompleted(StepEvent event, Map<String, Object> payload) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).stopStep(CorrelationPath.of(theCurrentScene), event.getStepPath(), payload);
        String stepPath = ParentPath.of(event.getStepPath());
        ((CorrelationState) theCurrentScene.recall(WireMockScreenplayContext.CORRELATION_STATE)).setCurrentStep(stepPath);
    }

    @ActorListener(involvement = ActorInvolvement.INTO_SPOTLIGHT)
    public void intoSpotlight(ActorEvent event) {
        logShineSpotlightOn(event);
        syncCorrelationState(event.getActorOnStage());
    }

    private void logShineSpotlightOn(ActorEvent event) {
        if (OnStage.theCurrentScene() instanceof ScenarioScope && inStep) {
            Scene theCurrentScene = theCurrentScene();
            Actor actor = event.getActorOnStage().getActor();
            String name = "Shine the spotlight on " + actor.getName();
            if (actor.getPersona().getUrl() != null) {
                name = "Shine the spotlight on **[" + actor.getName() + "](" + actor.getPersona().getUrl() + ")**";
            }
            Step step = new Step(null, "shineSpotlightOn", name, null, null, null);
            Map<String, Object> stepAndMatch = step.toMap();
            List<Argument> arguments = Collections.EMPTY_LIST;
            Match match = new Match(arguments, "");
            stepAndMatch.put("match", match.toMap());
            stepAndMatch.put("personaUrl", actor.getPersona().getUrl());
            stepAndMatch.put("method", "childStepAndMatch");
            getWireMockFrom(theCurrentScene).startStep(CorrelationPath.of(theCurrentScene), "shineSpotlightOn", stepAndMatch);
            Map<String, Object> result = new Result(Result.PASSED, 0l, null).toMap();
            result.put("method", "childResult");
            getWireMockFrom(theCurrentScene).stopStep(CorrelationPath.of(theCurrentScene), "shineSpotlightOn", result);
        }
    }

    //    @ActorListener(involvement = ActorInvolvement.INTO_SPOTLIGHT)
    private void syncCorrelationState(ActorOnStage userInScope) {
        if (!BaseActorOnStage.isEverybody(userInScope)) {
            CorrelationState state = userInScope.recall(WireMockScreenplayContext.CORRELATION_STATE);
            WireMockCorrelationState currentCorrelationState = DependencyInjectionAdaptorFactory.getAdaptor().getCurrentCorrelationState();
            currentCorrelationState.clear();
            currentCorrelationState.set(state.getCorrelationPath(), 1, Boolean.TRUE.equals(userInScope.recall(WireMockScreenplayContext.PROXY_UNMAPPED_ENDPOINTS)));
            for (ServiceInvocationCount entry : state.getServiceInvocationCounts()) {
                currentCorrelationState.initSequenceNumberFor(new com.sbg.bdd.wiremock.scoped.integration.ServiceInvocationCount(entry.toString()));
            }
        }
    }

    @ActorListener(involvement = ActorInvolvement.BEFORE_ENTER_STAGE)
    public void registerScope(ActorOnStage userInScope) {
        if (!BaseActorOnStage.isEverybody(userInScope)) {
            String parentScopePath = CorrelationPath.of(userInScope.getScene());
            CorrelationState state = getWireMockFrom(userInScope.getScene()).startUserScope(parentScopePath, userInScope.getId(), Collections.<String, Object>emptyMap());
            userInScope.remember(WireMockScreenplayContext.CORRELATION_STATE, state);
        }
    }

    private ScopedWireMockClient getWireMockFrom(Scene scene) {
        return scene.getPerformance().recall(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT);
    }
}
