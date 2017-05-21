package cucumber.screenplay.formatter;

import cucumber.runtime.StopWatch;
import cucumber.screenplay.*;
import cucumber.screenplay.Actor;
import gherkin.formatter.model.Result;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class FormattingActor implements Actor {
    private final String name;
    protected ActorOnStage currentRole;
    private Memory memory = new SimpleMemory();

    public void setCurrentRole(ActorOnStage currentRole) {
        this.currentRole = currentRole;
    }

    private static StopWatch stopWatch = new StopWatch() {
        final ThreadLocal<Deque<Long>> startStack = new ThreadLocal<>();

        @Override
        public void start() {
            if (startStack.get() == null) {
                startStack.set(new ArrayDeque<Long>());
            }
            startStack.get().push(System.nanoTime());
        }

        @Override
        public long stop() {
            return System.nanoTime() - startStack.get().pollLast();
        }
    };
    private Set<Ability> abilities = new HashSet<>();

    public FormattingActor(String name) {
        this.name = name;
    }

    public static void useStopWatch(StopWatch stopWatch) {
        FormattingActor.stopWatch = stopWatch;
    }

    public static FormattingActor named(String name) {
        return new FormattingActor(name);
    }

    @Override
    public void wasAbleTo(Performable... todos) {
        perform("Was able to", todos);
    }

    @Override
    public void attemptsTo(Performable... tasks) {
        perform("Attempts to", tasks);
    }

    @Override
    public void should(Consequence... consequences) {
        performSteps(extractInfo("Should", consequences, this));
    }

    public void perform(String keyword, Performable... steps) {
        performSteps(extractInfo(keyword, steps, this));
    }

    public static ChildStepInfo[] extractInfo(String keyword, Object[] steps, Object performer) {
        ChildStepInfo[] result = new ChildStepInfo[steps.length];
        for (int i = 0; i < steps.length; i++) {
            result[i] = new ChildStepInfo(keyword, steps[i], performer);
        }
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void can(Ability doSomething) {
        abilities.add(doSomething);
    }

    public void performSteps(ChildStepInfo... steps) {
        StepErrorTally errorTally = new StepErrorTally(stopWatch);
        for (ChildStepInfo childStepInfo : steps) {
            errorTally.startStopWatch();
            try {
                getFormatter().childStep(childStepInfo.getStep(), childStepInfo.getMatch());
                if (errorTally.shouldSkip() || childStepInfo.isSkipped()) {
                    logEmbeddingsAndResult(childStepInfo, Result.SKIPPED);
                } else if (childStepInfo.isPending()) {
                    errorTally.setPending();
                    logEmbeddingsAndResult(childStepInfo, new Result("pending", errorTally.stopStopWatch(), null));
                } else {
                    childStepInfo.invoke();
                    logEmbeddingsAndResult(childStepInfo, new Result(Result.PASSED, errorTally.stopStopWatch(), null));
                }
            } catch (Throwable t) {
                logEmbeddingsAndResult(childStepInfo, errorTally.respondTo(t));
            }
        }
        errorTally.reportAnyErrors();
    }

    private void logEmbeddingsAndResult(ChildStepInfo childStepInfo, Result skipped) {
        logEmbeddings(childStepInfo);
        getFormatter().childResult(skipped);
    }

    private void logEmbeddings(ChildStepInfo csi) {
        for (Pair<String, byte[]> embedding : Embeddings.producedBy(csi.getImplementation())) {
            getFormatter().embedding(embedding.getKey(), embedding.getValue());
        }
    }

    private ScreenPlayFormatter getFormatter() {
        return ScreenPlayFormatter.getCurrent();
    }

    @Override
    public void remember(String name, Object value) {
        getMemoryToUse().remember(name, value);
    }

    @Override
    public void remember(Object value) {
        getMemoryToUse().remember(value);
    }

    private Memory getMemoryToUse() {
        return currentRole != null ? currentRole : memory;
    }

    @Override
    public void forget(String name) {
        getMemoryToUse().forget(name);
    }

    @Override
    public <T> T recall(String name) {
        return getMemoryToUse().recall(name);
    }

    @Override
    public <T> T recall(Class<T> clzz) {
        return getMemoryToUse().recall(clzz);
    }
}
