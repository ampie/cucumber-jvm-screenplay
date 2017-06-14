package com.sbg.bdd.cucumber.screenplay.core.formatter;

import com.sbg.bdd.cucumber.common.ReportingFormatter;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import gherkin.formatter.model.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;


public class CucumberScreenplayLifecycleSync implements ReportingFormatter {
    Deque<Step> stepQueue=new ArrayDeque<>();

    public CucumberScreenplayLifecycleSync() {
    }

    @Override
    public void uri(String featureUri) {

    }

    @Override
    public void feature(Feature f) {

    }

    @Override
    public void startOfScenarioLifeCycle(Scenario s) {
        OnStage.raiseTheCurtain(s.getName());
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        OnStage.drawTheCurtain();
    }

    @Override
    public void done() {

    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }


    @Override
    public void result(Result result) {
    }

    @Override
    public void match(Match match) {
        Step step = stepQueue.removeFirst();
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
        stepQueue.add(step);
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

    @Override
    public void childStep(Step step, Match match) {

    }

    @Override
    public void childResult(Result result) {

    }
}
