package com.sbg.bdd.cucumber.screenplay.scoped.plugin;

import com.sbg.bdd.cucumber.screenplay.core.formatter.ReportingFormatter;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.scoped.*;
import cucumber.runtime.StepDefinitionMatch;
import gherkin.formatter.model.*;

import java.util.List;


public class CucumberScopeLifecycleSync implements ReportingFormatter {
    private String currentStep;
    private boolean hasRunRootScope;
    private String currentUri;
    private String featureName;

    public CucumberScopeLifecycleSync() {
    }

    @Override
    public void uri(String featureUri) {
        this.currentUri = featureUri;
    }

    @Override
    public void feature(Feature f) {
        featureName = f.getName();
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario s) {
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
    }

    @Override
    public void done() {
        getGlobalScope().complete();
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }


    @Override
    public void result(Result result) {
        getInnerMostActive(ScenarioScope.class).completeStep(currentStep);
    }

    @Override
    public void match(Match match) {
        try {
            if (match instanceof StepDefinitionMatch) {
                StepDefinitionMatch sdm = (StepDefinitionMatch) match;
                this.currentStep = sdm.getStepName();
                getInnerMostActive(ScenarioScope.class).startStep(currentStep);
            }
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

    }


    @Override
    public void background(Background background) {

    }

    @Override
    public void scenario(Scenario scenario) {

    }

    @Override
    public void step(Step step) {

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
}
