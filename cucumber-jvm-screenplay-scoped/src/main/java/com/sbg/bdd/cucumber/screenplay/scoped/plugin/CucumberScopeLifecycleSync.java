package com.sbg.bdd.cucumber.screenplay.scoped.plugin;

import com.sbg.bdd.cucumber.screenplay.core.formatter.StepQueueingReportingFormatter;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.annotations.SceneEventType;
import com.sbg.bdd.screenplay.core.annotations.StepEventType;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.internal.BaseActor;
import com.sbg.bdd.screenplay.scoped.*;
import gherkin.formatter.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CucumberScopeLifecycleSync extends StepQueueingReportingFormatter {
    private static CucumberScopeLifecycleSync instance;
    private boolean hasRunRootScope;
    private String currentUri;
    private String featureName;
    private GherkinStepMethodInfo currentStep;
    private Map<String, StepEventType> stepEventTypeMap = new HashMap<>();
    private Feature currentFeature;
    private BasicStatement currentFeatureElement;
    private Scenario currentScenarioLifecycle;
    private Examples currentExamples;

    {
        stepEventTypeMap.put("pending", StepEventType.PENDING);
        stepEventTypeMap.put(Result.FAILED, StepEventType.FAILED);
        stepEventTypeMap.put(Result.PASSED, StepEventType.SUCCESSFUL);
        stepEventTypeMap.put(Result.SKIPPED.getStatus(), StepEventType.SKIPPED);
        stepEventTypeMap.put(Result.UNDEFINED.getStatus(), StepEventType.PENDING);
    }

    public CucumberScopeLifecycleSync() {
        instance=this;
    }

    public static CucumberScopeLifecycleSync getInstance() {
        return instance;
    }

    public BasicStatement getCurrentFeatureElement() {
        return currentFeatureElement;
    }

    public Examples getCurrentExamples() {
        return currentExamples;
    }

    public Scenario getCurrentScenarioLifecycle() {
        return currentScenarioLifecycle;
    }

    public Feature getCurrentFeature() {
        return currentFeature;
    }

    @Override
    public void uri(String featureUri) {
        this.currentUri = featureUri;
    }

    @Override
    public void feature(Feature f) {
        featureName = f.getName();
        this.currentFeature=f;
    }

    @Override
    public void background(Background background) {
        this.currentFeatureElement=background;
        getGlobalScope().getEventBus().broadcast(new SceneEvent(getInnerMostActive(ScenarioScope.class), SceneEventType.ON_PHASE_ENTERED,"background"));
    }

    @Override
    public void scenario(Scenario scenario) {
        this.currentFeatureElement=scenario;
        getGlobalScope().getEventBus().broadcast(new SceneEvent(getInnerMostActive(ScenarioScope.class), SceneEventType.ON_PHASE_ENTERED,"scenario"));
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        super.scenarioOutline(scenarioOutline);
        this.currentFeatureElement=scenarioOutline;
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario s) {
        super.startOfScenarioLifeCycle(s);
        currentScenarioLifecycle=s;
        try {
            if (featureName != null) {
                scenarioContainer(featureName);
                featureName = null;
            }
            getInnerMostActive(FunctionalScope.class).startScenario(s.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        ScenarioScope srs = getInnerMostActive(ScenarioScope.class);
        getInnerMostActive(FunctionalScope.class).completeNestedScope(srs.getName());
        currentScenarioLifecycle=null;
    }
    @Override
    public void done() {
        if(getGlobalScope()!=null) {
            getGlobalScope().complete();
        }
    }


    @Override
    public void result(Result result) {
        long duration = result.getDuration() == null ? 0 : result.getDuration();
        StepEvent event = new StepEvent(null, currentStep, stepEventTypeMap.get(result.getStatus()), duration, result.getError());
        //just fire the event, the ScreenplaytLifecycleSync class will do the rest
        getGlobalScope().getEventBus().broadcast(event);
    }

    @Override
    public void stepMatch(final Step step, final Match match) {
        try {
            this.currentStep = new GherkinStepMethodInfo(step, match);
            BaseActor.setCurrentStep(this.currentStep.getName());
            //just fire the event, the ScreenplaytLifecycleSync class will do the rest
            StepEvent event = new StepEvent(null, currentStep,StepEventType.STARTED);
            //just fire the event, the ScreenplaytLifecycleSync class will do the rest
            getGlobalScope().getEventBus().broadcast(event);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private VerificationScope getCurrentScope() {
        return getGlobalScope().getInnerMostActive(VerificationScope.class);
    }

    private GlobalScope getGlobalScope() {
        return (GlobalScope) OnStage.performance();
    }

    private void scenarioContainer(String scenarioContainerName) {
        if (!hasRunRootScope) {
            getGlobalScope().start();
            hasRunRootScope = true;

        }
        String featureName = relativeBaseFileName(scenarioContainerName);
        String[] path = featureName.split("/");
        int pathLevelOfMostCommonParent = performCleanups(path);
        prepareChildren(path, pathLevelOfMostCommonParent + 1);
    }

    private VerificationScope prepareChildren(String[] path, int level) {
        VerificationScope scope = getGlobalScope().getInnerMostActive(UserTrackingScope.class);
        for (int i = level; i < path.length; i++) {
            String currentName = path[i];
            if (scope instanceof FunctionalScope) {
                VerificationScope functionalRunScope = ((FunctionalScope) scope).startNestedScope(currentName);
                scope = functionalRunScope;
            } else if (scope instanceof GlobalScope) {
                VerificationScope functionalRunScope = ((GlobalScope) scope).startFunctionalScope(currentName);
                scope = functionalRunScope;
            }
        }
        return scope;
    }

    private int performCleanups(String[] path) {
        VerificationScope scope = getCurrentScope();
        if (scope != null) {
            for (int i = path.length - 1; i >= 0; i--) {
                String currentId = path[i];
                if (scope.getLevel() == 0) {
                    return -1;
                } else if (scope.getId().equals(currentId)) {
                    //TODO check more accurately - maybe check if parent's name is also what it should be etc.
                    return i;
                } else {
                    if (scope.getContainingScope() != null) {
                        scope.getContainingScope().completeNestedScope(scope.getName());
                    }
                    scope = scope.getContainingScope();
                }
            }
        }
        return -1;
    }

    private String relativeBaseFileName(String name) {
        if (currentUri.lastIndexOf('/') == -1) {
            return name;
        } else {
            return currentUri.substring(0, currentUri.lastIndexOf('/')) + '/' + name;
        }
    }

    private <T> T getInnerMostActive(Class<T> scopeType) {
        return getGlobalScope().getInnerMostActive(scopeType);
    }


    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {

    }


    @Override
    public void examples(Examples examples) {
        currentExamples=examples;
    }



    @Override
    public void close() {

    }

    @Override
    public void eof() {

    }

    @Override
    public void before(Match match, Result result) {

    }


    @Override
    public void after(Match match, Result result) {

    }


    @Override
    public void embedding(String mimeType, byte[] data) {

    }

    @Override
    public void write(String text) {

    }

    public String getCurrentUri() {
        return currentUri;
    }

    @Override
    public void childStep(Step step, Match match) {

    }

    @Override
    public void childResult(Result result) {

    }
}
