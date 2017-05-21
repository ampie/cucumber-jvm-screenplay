package cucumber.screenplay.formatter;

import gherkin.formatter.JSONFormatter;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

public class CopiedCucumberJSONFormatter extends CopiedJSONFormatter {
    private boolean inScenarioOutline = false;

    public CopiedCucumberJSONFormatter(Appendable out) {
        super(out);
    }

    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        this.inScenarioOutline = true;
    }

    public void examples(Examples examples) {
    }

    public void startOfScenarioLifeCycle(Scenario scenario) {
        this.inScenarioOutline = false;
        super.startOfScenarioLifeCycle(scenario);
    }

    public void step(Step step) {
        if (!this.inScenarioOutline) {
            super.step(step);
        }

    }
}