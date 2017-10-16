package com.sbg.bdd.cucumber.screenplay.core.formatter;

import gherkin.formatter.model.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

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
    public void result(Result result) {

    }

    @Override
    public void after(Match match, Result result) {

    }

    @Override
    public void embedding(String s, byte[] bytes) {

    }

    @Override
    public void write(String s) {

    }

    @Override
    public void childStep(Step step, Match match) {

    }

    @Override
    public void childResult(Result result) {

    }

    @Override
    public void syntaxError(String s, String s1, List<String> list, String s2, Integer integer) {

    }

    @Override
    public void uri(String s) {

    }

    @Override
    public void feature(Feature feature) {

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
    public void endOfScenarioLifeCycle(Scenario scenario) {

    }

    @Override
    public void done() {

    }
}
