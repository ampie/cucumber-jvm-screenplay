package com.sbg.bdd.cucumber.screenplay.scoped.plugin;

import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.annotations.SceneEventType;
import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.core.annotations.StepEventType;
import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.internal.Attatchments;
import com.sbg.bdd.screenplay.core.internal.ScreenplayStepMethodInfo;
import com.sbg.bdd.screenplay.scoped.FunctionalScope;
import com.sbg.bdd.screenplay.scoped.ScenarioScope;
import gherkin.deps.net.iharder.Base64;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sbg.bdd.cucumber.screenplay.core.formatter.FormattingStepListener.extractArguments;
import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;

/*
TODO refactor this by decoupling the Cucumber dependencies.
Maybe put the payload on the Scope object's own memory and do this from the CucumberScopeLifecycleSync
Unfortunately the nested steps is where it all becomes tricky. Might need some kind of PayLoadStrategy there
 */
public abstract class CucumberPayloadProducingListener {
    protected boolean inStep = false;

    protected abstract void scopeStarted(Scene scene);

    protected abstract void featureStarted(FunctionalScope scene, Map<String, Object> map);

    protected abstract void scenarioPhaseEntered(ScenarioScope scene, Map<String, Object> payload);

    protected abstract void stepStarted(StepEvent event, Map<String, Object> payload);

    protected abstract void stepCompleted(StepEvent event, Map<String, Object> payload);


    @SceneListener(scopePhases = SceneEventType.ON_PHASE_ENTERED)
    public void scenarioPhaseEntered(SceneEvent event) {
        CucumberScopeLifecycleSync sync = CucumberScopeLifecycleSync.getInstance();
        if (event.getScene() instanceof ScenarioScope) {
            Map<String, Object> map = sync.getCurrentFeatureElement().toMap();
            map.put("method", "featureElement");
            scenarioPhaseEntered((ScenarioScope) event.getScene(), map);
        }
    }


    @SceneListener(scopePhases = SceneEventType.BEFORE_START)
    public void potentialFeatureStarted(Scene scene) {
        CucumberScopeLifecycleSync sync = CucumberScopeLifecycleSync.getInstance();
        if (!(scene instanceof ScenarioScope) && scene.getName().equals(sync.getCurrentFeature().getName())) {
            Map<String, Object> map = sync.getCurrentFeature().toMap();
            map.put("uri", sync.getCurrentUri());
            map.put("method", "feature");
            featureStarted((FunctionalScope) scene, map);
        } else {
            scopeStarted(scene);
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
        stepStarted(event, stepAndMatch);
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
        stepCompleted(event, map);
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
