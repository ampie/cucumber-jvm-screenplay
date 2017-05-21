package cucumber.scoping.plugin;

import cucumber.api.java8.GlueBase;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.scoping.*;
import cucumber.screenplay.actors.OnStage;
import cucumber.screenplay.formatter.ScreenPlayFormatter;
import gherkin.formatter.model.*;


public class ScopingFormatter extends ScreenPlayFormatter implements GlueBase {
    private String currentStep;
    private boolean hasRunRootScope;
    private String currentUri;
    private String featureName;

    public ScopingFormatter(Appendable out) {
        super(out);

    }

    @Override
    public void uri(String featureUri) {
        super.uri(featureUri);
        this.currentUri = featureUri;
    }

    @Override
    public void feature(Feature f) {
        super.feature(f);
        featureName = f.getName();
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario s) {
        try {

            super.startOfScenarioLifeCycle(s);
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
        super.endOfScenarioLifeCycle(scenario);
        ScenarioScope srs = getInnerMostActive(ScenarioScope.class);
        getInnerMostActive(FunctionalScope.class).completeNestedScope(srs.getName());
    }

    @Override
    public void done() {
        super.done();
        getGlobalScope().complete();
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        super.scenarioOutline(scenarioOutline);
    }

    @Override
    public void childStep(Step step, Match match) {
        super.childStep(step, match);
        getInnerMostActive(StepScope.class).startChildStep(step.getName());
    }

    @Override
    public void childResult(Result result) {
        super.childResult(result);
        StepScope step = getInnerMostActive(StepScope.class);
        VerificationScope containingScope = getInnerMostActive(StepScope.class).getContainingScope();
        ((StepScope) containingScope).completeChildStep(step.getName());
    }

    @Override
    public void result(Result result) {
        super.result(result);
        getInnerMostActive(ScenarioScope.class).completeStep(currentStep);
    }

    @Override
    public void match(Match match) {
        super.match(match);
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
        if (!hasRunRootScope && getCurrentScope().getLevel() == 0) {
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
}
