package com.sbg.bdd.cucumber.screenplay.core.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

public interface ReportingFormatter extends Formatter, Reporter {
    void childStep(Step step, Match match);

    void childResult(Result result);
}
