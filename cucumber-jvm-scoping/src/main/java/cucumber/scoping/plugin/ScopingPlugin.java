package cucumber.scoping.plugin;

import cucumber.screenplay.formatter.ScreenPlayFormatter;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.util.ArrayList;
import java.util.List;


public class ScopingPlugin implements Reporter, Formatter {
    List<Reporter> reporters = new ArrayList<>();
    List<Formatter> formatters = new ArrayList<>();

    public ScopingPlugin(Appendable out) {
        ScreenPlayFormatter e = new ScreenPlayFormatter(out);
        formatters.add(e);
        reporters.add(e);
        CucumberLifecycleSync e1 = new CucumberLifecycleSync();
        formatters.add(e1);
        reporters.add(e1);

    }

    @Override
    public void uri(String featureUri) {
        for (Formatter formatter : formatters) {
            formatter.uri(featureUri);
        }
    }

    @Override
    public void feature(Feature f) {
        for (Formatter formatter : formatters) {
            formatter.feature(f);
        }
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario s) {
        for (Formatter formatter : formatters) {
            formatter.startOfScenarioLifeCycle(s);
        }
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        for (Formatter formatter : formatters) {
            formatter.endOfScenarioLifeCycle(scenario);
        }
    }

    @Override
    public void done() {
        for (Formatter formatter : formatters) {
            formatter.done();
        }
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        for (Formatter formatter : formatters) {
            formatter.scenarioOutline(scenarioOutline);
        }
    }

    @Override
    public void result(Result result) {
        for (Reporter reporter : reporters) {
            reporter.result(result);
        }
    }

    @Override
    public void match(Match match) {
        for (Reporter reporter : reporters) {
            reporter.match(match);
        }
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        for (Formatter formatter : formatters) {
            formatter.syntaxError(state, event, legalEvents, uri, line);
        }
    }

    @Override
    public void examples(Examples examples) {
        for (Formatter formatter : formatters) {
            formatter.examples(examples);
        }
    }

    @Override
    public void background(Background background) {
        for (Formatter formatter : formatters) {
            formatter.background(background);
        }
    }

    @Override
    public void scenario(Scenario scenario) {
        for (Formatter formatter : formatters) {
            formatter.scenario(scenario);
        }
    }

    @Override
    public void step(Step step) {
        for (Formatter formatter : formatters) {
            formatter.step(step);
        }
    }

    @Override
    public void close() {
        for (Formatter formatter : formatters) {
            formatter.close();
        }
    }

    @Override
    public void eof() {
        for (Formatter formatter : formatters) {
            formatter.eof();
        }
    }

    @Override
    public void before(Match match, Result result) {
    }

    @Override
    public void after(Match match, Result result) {
        for (Reporter reporter : reporters) {
            reporter.after(match, result);
        }
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        for (Reporter reporter : reporters) {
            reporter.embedding(mimeType, data);
        }
    }

    @Override
    public void write(String text) {
        for (Reporter reporter : reporters) {
            reporter.write(text);
        }
    }
}
