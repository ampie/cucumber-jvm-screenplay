package com.sbg.bdd.cucumber.screenplay.core.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MultiPlugin implements ReportingFormatter {
    List<ReportingFormatter> delegates = new ArrayList<>();

    public MultiPlugin(ReportingFormatter ... delegates) {
        this.delegates= Arrays.asList(delegates);
    }

    @Override
    public void childStep(Step step, Match match) {

    }

    @Override
    public void childResult(Result result) {

    }

    @Override
    public void uri(String featureUri) {
        for (Formatter formatter : delegates) {
            formatter.uri(featureUri);
        }
    }

    @Override
    public void feature(Feature f) {
        for (Formatter formatter : delegates) {
            formatter.feature(f);
        }
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario s) {
        for (Formatter formatter : delegates) {
            formatter.startOfScenarioLifeCycle(s);
        }
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        for (Formatter formatter : delegates) {
            formatter.endOfScenarioLifeCycle(scenario);
        }
    }

    @Override
    public void done() {
        for (Formatter formatter : delegates) {
            formatter.done();
        }
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        for (Formatter formatter : delegates) {
            formatter.scenarioOutline(scenarioOutline);
        }
    }

    @Override
    public void result(Result result) {
        for (Reporter reporter : delegates) {
            reporter.result(result);
        }
    }

    @Override
    public void match(Match match) {
        for (Reporter reporter : delegates) {
            reporter.match(match);
        }
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        for (Formatter formatter : delegates) {
            formatter.syntaxError(state, event, legalEvents, uri, line);
        }
    }

    @Override
    public void examples(Examples examples) {
        for (Formatter formatter : delegates) {
            formatter.examples(examples);
        }
    }

    @Override
    public void background(Background background) {
        for (Formatter formatter : delegates) {
            formatter.background(background);
        }
    }

    @Override
    public void scenario(Scenario scenario) {
        for (Formatter formatter : delegates) {
            formatter.scenario(scenario);
        }
    }

    @Override
    public void step(Step step) {
        for (Formatter formatter : delegates) {
            formatter.step(step);
        }
    }

    @Override
    public void close() {
        for (Formatter formatter : delegates) {
            formatter.close();
        }
    }

    @Override
    public void eof() {
        for (Formatter formatter : delegates) {
            formatter.eof();
        }
    }

    @Override
    public void before(Match match, Result result) {
    }

    @Override
    public void after(Match match, Result result) {
        for (Reporter reporter : delegates) {
            reporter.after(match, result);
        }
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        for (Reporter reporter : delegates) {
            reporter.embedding(mimeType, data);
        }
    }

    @Override
    public void write(String text) {
        for (Reporter reporter : delegates) {
            reporter.write(text);
        }
    }

}
