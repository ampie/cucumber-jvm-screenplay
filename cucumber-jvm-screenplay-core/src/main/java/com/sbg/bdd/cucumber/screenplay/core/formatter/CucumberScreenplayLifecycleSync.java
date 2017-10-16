package com.sbg.bdd.cucumber.screenplay.core.formatter;

import com.sbg.bdd.screenplay.core.actors.OnStage;
import gherkin.formatter.model.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;


public class CucumberScreenplayLifecycleSync extends  StepQueueingReportingFormatter {

    public CucumberScreenplayLifecycleSync() {
    }
    @Override
    public void startOfScenarioLifeCycle(Scenario s) {
        OnStage.raiseTheCurtain(s.getName());
    }

    @Override
    public void stepMatch(Step step, Match match) {
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        OnStage.drawTheCurtain();
    }
}
