package com.sbg.bdd.cucumber.screenplay.core.formatter;

import com.sbg.bdd.cucumber.common.ReportingFormatter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class StepQueueingReportingFormatter implements ReportingFormatter {
    protected Deque<Step> stepQueue= new ArrayDeque<>();
    private boolean processOutlineSteps;

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        this.processOutlineSteps = true;
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        this.processOutlineSteps=false;
    }

    @Override
    public final void step(Step step) {
        if(!processOutlineSteps){
            stepQueue.addLast(step);
        }
    }

    @Override
    public final void match(Match match) {
        stepMatch(stepQueue.pop(),match);
    }
    public abstract void stepMatch(Step step, Match match);
}
