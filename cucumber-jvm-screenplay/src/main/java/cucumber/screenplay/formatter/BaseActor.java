package cucumber.screenplay.formatter;

import cucumber.screenplay.*;
import cucumber.screenplay.internal.ChildStepInfo;
import cucumber.screenplay.internal.SimpleMemory;
import cucumber.screenplay.internal.StopWatch;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseActor<T extends ChildStepInfo> implements Actor {
    protected static StopWatch stopWatch = new StopWatch() {
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
    protected final String name;
    protected ActorOnStage currentRole;
    private Memory memory = new SimpleMemory();
    private Set<Ability> abilities = new HashSet<>();

    public BaseActor(String name) {
        this.name = name;
    }

    protected abstract T[] extractInfo(String keyword, Object performer, Object[] stepObjects);

    protected abstract void logChildStepStart(T childStepInfo);

    protected abstract void logChildStepResult(StepErrorTally errorTally, T childStepInfo);

    protected abstract void logChildStepPending(StepErrorTally errorTally, T childStepInfo);

    protected abstract void logChildStepSkipped(T childStepInfo);

    protected abstract void logChildStepFailure(T childStepInfo, StepErrorTally errorTally, Throwable skipped);

    @Override
    public void should(Consequence... consequences) {
        performSteps("Should", this, consequences);
    }

    @Override
    public void wasAbleTo(Performable... todos) {
        performSteps("Was able to", this, todos);
    }

    @Override
    public void attemptsTo(Performable... tasks) {
        performSteps("Attempts to", this, tasks);
    }

    public void performSteps(String keyword, Object performer, Object... stepObjects) {
        T[] steps = extractInfo(keyword, performer, stepObjects);
        StepErrorTally errorTally = new StepErrorTally(stopWatch);
        for (T childStepInfo : steps) {
            errorTally.startStopWatch();
            try {
                logChildStepStart(childStepInfo);
                if (errorTally.shouldSkip() || childStepInfo.isSkipped()) {
                    logChildStepSkipped(childStepInfo);
                } else if (childStepInfo.isPending()) {
                    errorTally.setPending();
                    logChildStepPending(errorTally, childStepInfo);
                } else {
                    childStepInfo.invoke();
                    logChildStepResult(errorTally, childStepInfo);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                logChildStepFailure(childStepInfo, errorTally, t);
            }
        }
        errorTally.reportAnyErrors();

    }


    public static void useStopWatch(StopWatch stopWatch) {
        BaseActor.stopWatch = stopWatch;
    }

    public void setCurrentRole(ActorOnStage currentRole) {
        this.currentRole = currentRole;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void can(Ability doSomething) {
        abilities.add(doSomething);
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
