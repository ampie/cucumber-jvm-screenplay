package com.sbg.bdd.screenplay.core.internal;

import com.sbg.bdd.screenplay.core.*;
import com.sbg.bdd.screenplay.core.annotations.StepEventType;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.events.StepEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class BaseActor implements Actor {
    protected static StopWatch stopWatch;
    protected final String name;
    private Memory memory = new SimpleMemory();
    private Set<Ability> abilities = new HashSet<>();
    private ScreenPlayEventBus eventBus;
    private Deque<String> stepPaths = new ArrayDeque<>();

    public BaseActor(ScreenPlayEventBus eventBus, String name) {
        this.eventBus = eventBus;
        this.name = name;
    }

    protected StepMethodInfo[] extractInfo(String keyword, Object performer, Object[] stepObjects) {
        StepMethodInfo[] result = new StepMethodInfo[stepObjects.length];
        for (int i = 0; i < stepObjects.length; i++) {
            result[i] = new StepMethodInfo(stepPaths.isEmpty() ? null : stepPaths.peekLast(), keyword, performer, stepObjects[i]);
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
        StepMethodInfo[] steps = extractInfo(keyword, performer, stepObjects);
        StepErrorTally errorTally = new StepErrorTally(getStopWatch());
        for (StepMethodInfo stepMethodInfo : steps) {
            errorTally.startStopWatch();
            try {
                pushStepPath(stepMethodInfo);
                eventBus.broadcast(new StepEvent(stepMethodInfo, StepEventType.STEP_STARTED));
                if (errorTally.shouldSkip() || stepMethodInfo.isSkipped()) {
                    eventBus.broadcast(new StepEvent(stepMethodInfo, StepEventType.STEP_SKIPPED, errorTally.stopStopWatch()));
                } else if (stepMethodInfo.isPending()) {
                    errorTally.setPending();
                    eventBus.broadcast(new StepEvent(stepMethodInfo, StepEventType.STEP_PENDING, errorTally.stopStopWatch()));
                } else {
                    stepMethodInfo.invoke();
                    eventBus.broadcast(new StepEvent(stepMethodInfo, StepEventType.STEP_SUCCESSFUL, errorTally.stopStopWatch()));
                }
            } catch (Throwable t) {
                errorTally.addThrowable(t);
                if (errorTally.indicatesPending(t)) {
                    eventBus.broadcast(new StepEvent(stepMethodInfo, StepEventType.STEP_PENDING, errorTally.stopStopWatch(), t));
                } else if (errorTally.indicatesAssertionFailed(t)) {
                    eventBus.broadcast(new StepEvent(stepMethodInfo, StepEventType.STEP_ASSERTION_FAILED, errorTally.stopStopWatch(), t));
                } else {
                    eventBus.broadcast(new StepEvent(stepMethodInfo, StepEventType.STEP_FAILED, errorTally.stopStopWatch(), t));
                }
            } finally {
                popStepPath();
            }
        }
        errorTally.reportAnyErrors();
    }

    private void popStepPath() {
        stepPaths.pollLast();
    }

    private void pushStepPath(StepMethodInfo stepMethodInfo) {
        if (stepPaths.isEmpty()) {
            stepPaths.offerLast(stepMethodInfo.getName());
        } else {
            stepPaths.offerLast(stepPaths.getLast() + "/" + stepMethodInfo.getName());
        }
    }

    private StopWatch getStopWatch() {
        if (stopWatch == null) {
            stopWatch = new StackedStopWatch();
        }
        return stopWatch;
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
