package com.sbg.bdd.cucumber.screenplay.scoped.plugin;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.annotations.*;
import com.sbg.bdd.screenplay.core.events.OnStageActorEvent;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.internal.Attatchments;
import com.sbg.bdd.screenplay.core.internal.BaseActorOnStage;
import com.sbg.bdd.screenplay.core.internal.ScreenplayStepMethodInfo;
import com.sbg.bdd.screenplay.scoped.FunctionalScope;
import com.sbg.bdd.screenplay.scoped.PayloadConsumingListener;
import com.sbg.bdd.screenplay.scoped.ScenarioScope;
import gherkin.deps.net.iharder.Base64;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static com.sbg.bdd.cucumber.screenplay.core.formatter.FormattingStepListener.extractArguments;
import static com.sbg.bdd.screenplay.core.actors.OnStage.theCurrentScene;
import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;

/**
 * This is an adapter that listens to Scoped Screenplay events, builds up a payload to associate with the event, and then
 * delegates it to a PayloadConsumingListener. The payload produced is expected to reflect the current Gherkin element being processed.
 * The PayloadConusmingListener will typically be responsible for generating a report of some sort, or to synchronize the Cucumber
 * scoped state with some other scope management framework.
 */
public abstract class CucumberPayloadProducingListener {
    protected boolean inStep = false;
    private PayloadConsumingListener payloadConsumingListener;
    public static final String UPDATE_JIRA = "serenity.jira.integration.enabled";
    public static final String[] COMPLETION_STATE = {UPDATE_JIRA};

    protected CucumberPayloadProducingListener(PayloadConsumingListener payloadConsumingListener) {
        this.payloadConsumingListener = payloadConsumingListener;
    }

    @SceneListener(scopePhases = SceneEventType.AFTER_COMPLETE)
    public void unregisterScope(Scene scene) {
        Map<String, Object> payload = new HashMap<>();
        for (String s : COMPLETION_STATE) {
            Object value = scene.recall(s);
            if (value != null) {
                payload.put(s, value);
            }
        }
        payloadConsumingListener.scopeCompleted(scene, payload);
    }
    @ActorInvolvementListener(involvement = ActorInvolvement.BEFORE_ENTER_STAGE)
    public void registerScope(ActorOnStage userInScope) {
        if (!BaseActorOnStage.isEverybody(userInScope)) {
            Map<String, Object> payload = Collections.<String, Object>emptyMap();
            payloadConsumingListener.beforeEnterStage(userInScope, payload);
        }
    }
    @SceneListener(scopePhases = SceneEventType.ON_PHASE_ENTERED)
    public void scenarioPhaseEntered(SceneEvent event) {
        CucumberScopeLifecycleSync sync = CucumberScopeLifecycleSync.getInstance();
        if (event.getScene() instanceof ScenarioScope) {
            Map<String, Object> map = sync.getCurrentFeatureElement().toMap();
            map.put("method", "featureElement");
            payloadConsumingListener.scenarioPhaseEntered((ScenarioScope) event.getScene(), map);
        }
    }


    @SceneListener(scopePhases = SceneEventType.BEFORE_START)
    public void potentialFeatureStarted(Scene scene) {
        CucumberScopeLifecycleSync sync = CucumberScopeLifecycleSync.getInstance();
        if (!(scene instanceof ScenarioScope) && scene.getName().equals(sync.getCurrentFeature().getName())) {
            Map<String, Object> map = sync.getCurrentFeature().toMap();
            map.put("uri", sync.getCurrentUri());
            map.put("method", "feature");
            payloadConsumingListener.featureStarted((FunctionalScope) scene, map);
        } else {
            payloadConsumingListener.scopeStarted(scene);
        }
    }


    @StepListener(eventTypes = StepEventType.STARTED)
    public void registerStep(StepEvent event) {
        Map<String, Object> stepAndMatch = null;
        if (OnStage.theCurrentScene() instanceof ScenarioScope) {
            //Don't generate events for features or capabilities
            if (event.getStepLevel() == 0 && event.getInfo() instanceof GherkinStepMethodInfo) {
                inStep = true;
                GherkinStepMethodInfo mi = (GherkinStepMethodInfo) event.getInfo();
                stepAndMatch = mi.getStep().toMap();
                stepAndMatch.put("match", mi.getMatch().toMap());
                stepAndMatch.put("method", "stepAndMatch");
            } else {
                Step step = new Step(null, event.getInfo().getKeyword(), event.getInfo().getName(), null, null, null);
                List<Argument> arguments = extractArguments(event.getInfo().getNameExpression(), ((ScreenplayStepMethodInfo) event.getInfo()).getImplementation());
                Match match = new Match(arguments, event.getInfo().getLocation());
                stepAndMatch = step.toMap();
                stepAndMatch.put("match", match.toMap());
                stepAndMatch.put("method", "childStepAndMatch");
            }
        }
        payloadConsumingListener.stepStarted(event, stepAndMatch);
    }

    @StepListener(eventTypes = {PENDING, SKIPPED, ASSERTION_FAILED, SUCCESSFUL, FAILED})
    public void unregisterStep(StepEvent event) {
        Map<String, Object> map = new Result(statusOf(event), event.getDuration(), event.getError(), null).toMap();
        if (event.getStepLevel() == 0) {
            map.put("method", "result");
            inStep = false;
        } else {
            List<Map<String, Object>> embeddings = new ArrayList<>();
            for (Pair<String, byte[]> embedding : Attatchments.producedBy(((ScreenplayStepMethodInfo) event.getInfo()).getImplementation())) {
                HashMap<String, Object> embeddingMap = new HashMap<>();
                embeddingMap.put("mime_type", embedding.getKey());
                embeddingMap.put("data", Base64.encodeBytes(embedding.getValue()));
                embeddings.add(embeddingMap);
            }
            map.put("embeddings", embeddings);
            map.put("method", "childResult");
        }
        payloadConsumingListener.stepCompleted(event, map);
    }
    @ActorInvolvementListener(involvement = ActorInvolvement.INTO_SPOTLIGHT)
    public void intoSpotlight(OnStageActorEvent event) {
        logShineSpotlightOn(event);
    }

    private void logShineSpotlightOn(OnStageActorEvent event) {
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
            stepAndMatch.put("method", "childStepAndMatch");
            stepAndMatch.put("personaUrl", actor.getPersona().getUrl());

            payloadConsumingListener.beforeIntoSpotlight(theCurrentScene, event.getActorOnStage(), stepAndMatch);
            Map<String, Object> result = new Result(Result.PASSED, 0l, null).toMap();
            result.put("method", "childResult");
            payloadConsumingListener.afterIntoSpotlight(theCurrentScene, event.getActorOnStage(), result);
        }
    }


    private String statusOf(StepEvent event) {
        switch (event.getType()) {
            case ASSERTION_FAILED:
            case FAILED:
                return Result.FAILED;
            case PENDING:
                return Result.UNDEFINED.getStatus();
            case SKIPPED:
                return Result.SKIPPED.getStatus();
        }
        return Result.PASSED;
    }

}
