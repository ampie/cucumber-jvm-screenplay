package cucumber.screenplay.formatter;

import cucumber.screenplay.internal.ChildStepInfo;
import cucumber.screenplay.util.Fields;
import cucumber.screenplay.util.StringConverter;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;


public class CucumberChildStepInfo extends ChildStepInfo {
    private List<Argument> arguments;
    private Step step;
    private Match match;

    public CucumberChildStepInfo(String keyword, Object origin, Object performer) {
        super(performer, origin);
        try {
            deriveState();
            step = new Step(null, keyword, name, null, null, null);
            match = new Match(arguments, location);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }



    public Step getStep() {
        return step;
    }

    public Match getMatch() {
        return match;
    }

    @Override
    protected void extractNameFrom(Method method) {
        super.extractNameFrom(method);
        this.arguments = new ArrayList<>();
        if (name.contains("#")) {
            Scanner scanner = new Scanner(name);
            scanner.useDelimiter(Pattern.compile("[^\\w\\#]"));
            while (scanner.hasNext()) {
                String next = scanner.next();
                if (next.startsWith("#")) {
                    Object fieldValue = Fields.of(getImplementation()).asMap().get(next.substring(1));
                    String stringValue = StringConverter.toString(fieldValue);
                    arguments.add(new Argument(null, stringValue));
                }
            }
        }
    }


    public Result buildResult(StepErrorTally errorTally, Throwable t) {
        errorTally.addThrowable(t);
        if (errorTally.indicatesPending(t)) {
            return new Result("pending", errorTally.stopStopWatch(), t, null);
        }
        return new Result(Result.FAILED, errorTally.stopStopWatch(), t, null);
    }
}
