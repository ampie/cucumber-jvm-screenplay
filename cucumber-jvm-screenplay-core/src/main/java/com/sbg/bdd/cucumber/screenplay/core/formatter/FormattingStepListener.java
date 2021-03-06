package com.sbg.bdd.cucumber.screenplay.core.formatter;

import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.internal.Attatchments;
import com.sbg.bdd.screenplay.core.internal.ScreenplayStepMethodInfo;
import com.sbg.bdd.screenplay.core.util.Fields;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Pattern;

import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;

public class FormattingStepListener {

    @StepListener(eventTypes = SUCCESSFUL)
    public void logChildStepResult(StepEvent event) {
        if (getFormatter() != null && event.getInfo() instanceof ScreenplayStepMethodInfo) {
            logEmbeddingsAndResult((ScreenplayStepMethodInfo) event.getInfo(), new Result(Result.PASSED, event.getDuration(), null));
        }
    }

    @StepListener(eventTypes = PENDING)
    public void logChildStepPending(StepEvent event) {
        if (getFormatter() != null) {
            logEmbeddingsAndResult((ScreenplayStepMethodInfo) event.getInfo(), new Result("pending", event.getDuration(), null));
        }
    }
    @StepListener(eventTypes = {FAILED, ASSERTION_FAILED})
    public void logChildFailed(StepEvent event) {
        if (getFormatter() != null) {
            logEmbeddingsAndResult((ScreenplayStepMethodInfo) event.getInfo(), new Result(Result.FAILED, event.getDuration(), event.getError(),null));
        }
    }

    @StepListener(eventTypes = SKIPPED)
    public void logChildStepSkipped(StepEvent event) {
        if (getFormatter() != null) {
            logEmbeddingsAndResult((ScreenplayStepMethodInfo) event.getInfo(), Result.SKIPPED);
        }
    }


    @StepListener(eventTypes = STARTED)
    public void logChildStepStart(StepEvent event) {
        if (getFormatter() != null && event.getInfo() instanceof ScreenplayStepMethodInfo) {
            Step step = new Step(null, event.getInfo().getKeyword(), event.getInfo().getName(), null, null, null);
            List<Argument> arguments = extractArguments(event.getInfo().getNameExpression(), ((ScreenplayStepMethodInfo) event.getInfo()).getImplementation());
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



    private void logEmbeddingsAndResult(ScreenplayStepMethodInfo stepMethodInfo, Result skipped) {
        if (getFormatter() != null) {
            logEmbeddings(stepMethodInfo);
            getFormatter().childResult(skipped);
        }
    }

    private void logEmbeddings(ScreenplayStepMethodInfo csi) {
        for (Pair<String, byte[]> embedding : Attatchments.producedBy(csi.getImplementation())) {
            getFormatter().embedding(embedding.getKey(), embedding.getValue());
        }
    }


    private ScreenPlayFormatter getFormatter() {
        return ScreenPlayFormatter.getCurrent();
    }
}
