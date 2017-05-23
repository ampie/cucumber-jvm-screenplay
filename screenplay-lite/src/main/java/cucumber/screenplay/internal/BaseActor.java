package cucumber.screenplay.internal;

import cucumber.screenplay.*;
import cucumber.screenplay.events.ScreenPlayEvent;
import cucumber.screenplay.events.ScreenPlayEventBus;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class BaseActor implements Actor {
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
    private Memory memory = new SimpleMemory();
    private Set<Ability> abilities = new HashSet<>();
    private ScreenPlayEventBus eventBus;

    public BaseActor(ScreenPlayEventBus eventBus, String name) {
        this.eventBus = eventBus;
        this.name = name;
    }

    protected ChildStepInfo[] extractInfo(String keyword, Object performer, Object[] stepObjects) {
        ChildStepInfo[] result = new ChildStepInfo[stepObjects.length];
        for (int i = 0; i < stepObjects.length; i++) {
            result[i] = new ChildStepInfo(keyword, performer, stepObjects[i]);
        }
        return result;
    }

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
        ChildStepInfo[] steps = extractInfo(keyword, performer, stepObjects);
        StepErrorTally errorTally = new StepErrorTally(stopWatch);
        for (ChildStepInfo childStepInfo : steps) {
            errorTally.startStopWatch();
            try {
                eventBus.broadcast(new ScreenPlayEvent(childStepInfo, ScreenPlayEvent.Type.STEP_STARTED));
                if (errorTally.shouldSkip() || childStepInfo.isSkipped()) {
                    eventBus.broadcast(new ScreenPlayEvent(childStepInfo, ScreenPlayEvent.Type.STEP_SKIPPED, errorTally.stopStopWatch()));
                } else if (childStepInfo.isPending()) {
                    errorTally.setPending();
                    eventBus.broadcast(new ScreenPlayEvent(childStepInfo, ScreenPlayEvent.Type.STEP_PENDING, errorTally.stopStopWatch()));
                } else {
                    childStepInfo.invoke();
                    eventBus.broadcast(new ScreenPlayEvent(childStepInfo, ScreenPlayEvent.Type.STEP_SUCCESSFUL, errorTally.stopStopWatch()));
                }
            } catch (Throwable t) {
                errorTally.addThrowable(t);
                if (errorTally.indicatesPending(t)) {
                    eventBus.broadcast(new ScreenPlayEvent(childStepInfo, ScreenPlayEvent.Type.STEP_PENDING, errorTally.stopStopWatch(), t));
                } else if (errorTally.indicatesAssertionFailed(t)) {
                    eventBus.broadcast(new ScreenPlayEvent(childStepInfo, ScreenPlayEvent.Type.STEP_ASSERTION_FAILED, errorTally.stopStopWatch(), t));
                } else {
                    eventBus.broadcast(new ScreenPlayEvent(childStepInfo, ScreenPlayEvent.Type.STEP_FAILED, errorTally.stopStopWatch(), t));
                }
            }
        }
        errorTally.reportAnyErrors();
    }


    public static void useStopWatch(StopWatch stopWatch) {
        BaseActor.stopWatch = stopWatch;
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
        return memory;
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
