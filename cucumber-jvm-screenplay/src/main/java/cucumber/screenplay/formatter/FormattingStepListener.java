package cucumber.screenplay.formatter;

import cucumber.screenplay.annotations.StepListener;
import cucumber.screenplay.events.ScreenPlayEvent;
import cucumber.screenplay.internal.ChildStepInfo;
import cucumber.screenplay.internal.Embeddings;
import cucumber.screenplay.util.Fields;
import cucumber.screenplay.util.StringConverter;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static cucumber.screenplay.events.ScreenPlayEvent.Type.*;

public class FormattingStepListener {

    @StepListener(eventTypes = STEP_SUCCESSFUL)
    public void logChildStepResult(ScreenPlayEvent event) {
        if (getFormatter() != null) {
            logEmbeddingsAndResult(event.getInfo(), new Result(Result.PASSED, event.getDuration(), null));
        }
    }

    @StepListener(eventTypes = STEP_PENDING)
    public void logChildStepPending(ScreenPlayEvent event) {
        if (getFormatter() != null) {
            logEmbeddingsAndResult(event.getInfo(), new Result("pending", event.getDuration(), null));
        }
    }
    @StepListener(eventTypes = {STEP_FAILED,STEP_ASSERTION_FAILED})
    public void logChildFailed(ScreenPlayEvent event) {
        if (getFormatter() != null) {
            logEmbeddingsAndResult(event.getInfo(), new Result(Result.FAILED, event.getDuration(), event.getError(),null));
        }
    }

    @StepListener(eventTypes = STEP_SKIPPED)
    public void logChildStepSkipped(ScreenPlayEvent event) {
        if (getFormatter() != null) {
            logEmbeddingsAndResult(event.getInfo(), Result.SKIPPED);
        }
    }


    @StepListener(eventTypes = STEP_STARTED)
    public void logChildStepStart(ScreenPlayEvent event) {
        if (getFormatter() != null) {
            Step step = new Step(null, event.getInfo().getKeyword(), event.getInfo().getName(), null, null, null);
            List<Argument> arguments = extractArguments(event.getInfo().getNameExpression(), event.getInfo().getImplementation());
            Match match = new Match(arguments, event.getInfo().getLocation());
            getFormatter().childStep(step, match);
        }
    }

    public static List<Argument> extractArguments(String name, Object implementation) {
        List<Argument> arguments=new ArrayList<>();
        if (name.contains("#")) {
            Scanner scanner = new Scanner(name);
            scanner.useDelimiter(Pattern.compile("[^\\w\\#]"));
            while (scanner.hasNext()) {
                String next = scanner.next();
                if (next.startsWith("#")) {
                    Object fieldValue = Fields.of(implementation).asMap().get(next.substring(1));
                    String stringValue = StringConverter.toString(fieldValue);
                    arguments.add(new Argument(null, stringValue));
                }
            }
        }
        return arguments;
    }



    private void logEmbeddingsAndResult(ChildStepInfo childStepInfo, Result skipped) {
        if (getFormatter() != null) {
            logEmbeddings(childStepInfo);
            getFormatter().childResult(skipped);
        }
    }

    private void logEmbeddings(ChildStepInfo csi) {
        for (Pair<String, byte[]> embedding : Embeddings.producedBy(csi.getImplementation())) {
            getFormatter().embedding(embedding.getKey(), embedding.getValue());
        }
    }


    private ScreenPlayFormatter getFormatter() {
        return ScreenPlayFormatter.getCurrent();
    }
}
