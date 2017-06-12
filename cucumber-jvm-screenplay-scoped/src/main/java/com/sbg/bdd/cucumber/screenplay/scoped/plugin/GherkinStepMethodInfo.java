package com.sbg.bdd.cucumber.screenplay.scoped.plugin;

import com.sbg.bdd.screenplay.core.internal.StepMethodInfo;
import cucumber.runtime.StepDefinitionMatch;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Step;

public class GherkinStepMethodInfo implements StepMethodInfo {
    private final Step step;
    private final Match match;

    public GherkinStepMethodInfo(Step step, Match match) {
        this.step = step;
        this.match = match;
    }

    public Step getStep() {
        return step;
    }

    public Match getMatch() {
        return match;
    }

    @Override
    public String getStepPath() {
        return getName();
    }

    @Override
    public String getKeyword() {
        return step.getKeyword();
    }

    @Override
    public String getNameExpression() {
        return ((StepDefinitionMatch) match).getPattern();
    }

    @Override
    public String getName() {
        return step.getName();
    }

    @Override
    public boolean isPending() {
        return false;
    }

    @Override
    public boolean isSkipped() {
        return false;
    }

    @Override
    public String getLocation() {
        return match.getLocation();
    }

    @Override
    public int getStepLevel() {
        return 0;
    }
}
