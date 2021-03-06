package com.sbg.bdd.cucumber.screenplay.core.formatter;

import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.util.*;

public class ScreenPlayFormatter extends CopiedJSONFormatter implements ReportingFormatter {
    private static ThreadLocal<ScreenPlayFormatter> current = new ThreadLocal<ScreenPlayFormatter>();
    private Deque<Map<String, Object>> childStepStack = new ArrayDeque<Map<String, Object>>();

    public ScreenPlayFormatter(Appendable out) {
        super(out);
        current.set(this);
    }

    public static ScreenPlayFormatter getCurrent() {
        return current.get();
    }

    @Override
    public void childStep(Step step, Match match) {
        Map currentStep = getCurrentStep(Phase.output);
        List<Map<String, Object>> children = (List<Map<String, Object>>) currentStep.get("children");
        if (children == null) {
            children = new ArrayList<Map<String, Object>>();
            currentStep.put("children", children);
        }
        Map<String, Object> stepAsMap = step.toMap();
        stepAsMap.put("match", match.toMap());
        children.add(stepAsMap);
        childStepStack.addLast(stepAsMap);
    }

    @Override
    public void childResult(Result result) {
        super.result(result);
        childStepStack.pollLast();
    }

    @Override
    protected Map getCurrentStep(Phase phase) {
        if (childStepStack.isEmpty()) {
            return super.getCurrentStep(phase);
        } else {
            Map<String, Object> peek = childStepStack.peekLast();
            return peek;
        }
    }
}
