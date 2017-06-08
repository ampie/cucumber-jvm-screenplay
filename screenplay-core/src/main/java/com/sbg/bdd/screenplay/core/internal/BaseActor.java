package com.sbg.bdd.screenplay.core.internal;

import com.sbg.bdd.screenplay.core.*;
import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.screenplay.core.annotations.StepEventType;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.util.NameConverter;

import java.util.*;

public class BaseActor implements Actor {
    protected static StopWatch stopWatch;
    protected final String name;
    private Memory memory = new SimpleMemory();
    private Map<Class<?>, Ability> abilities = new HashMap<>();
    private ScreenPlayEventBus eventBus;
    private Deque<String> stepPaths = new ArrayDeque<>();
    private String precedingKeyword;

    public BaseActor(ScreenPlayEventBus eventBus, String name) {
        this.eventBus = eventBus;
        this.name = name;
    }

    @Override
    public String getPrecedingKeyword() {
        return precedingKeyword;
    }

    protected StepMethodInfo[] extractInfo(String methodName, Object performer, Object[] stepObjects) {
        StepMethodInfo[] result = new StepMethodInfo[stepObjects.length];
        for (int i = 0; i < stepObjects.length; i++) {
            result[i] = new StepMethodInfo(getParentStepPath(), methodName, performer, stepObjects[i]);
        }
        return result;
    }

    String getParentStepPath() {
        return stepPaths.isEmpty() ? null : stepPaths.peekLast();
    }

    public void useKeyword(String format) {
        this.precedingKeyword = format;
    }

    @Override
    public void should(final Consequence... consequences) {
        performSteps(new StepMethodInfo[]{new StepMethodInfo(getParentStepPath(), "should", this, new Object() {
            String keyword = BaseActor.this.precedingKeyword;
            String name = BaseActor.this.name;
            @Step("#keyword #name should ")
            public void should(Actor me) {
                performSteps("evaluateFor", BaseActor.this, consequences);
            }
        })});
    }

    @Override
    public void wasAbleTo(final Performable... tasks) {
        performSteps(new StepMethodInfo[]{new StepMethodInfo(getParentStepPath(), "wasAbleTo", this, new Object() {
            String keyword = BaseActor.this.precedingKeyword;
            String name = BaseActor.this.name;
            @Step("#keyword #name was able to ")
            public void wasAbleTo(Actor me) {
                performSteps("performAs", BaseActor.this, tasks);
            }
        })});
    }

    @Override
    public void attemptsTo(final Performable... tasks) {
        performSteps(new StepMethodInfo[]{new StepMethodInfo(getParentStepPath(), "attemptsTo", this, new Object() {
            String keyword = BaseActor.this.precedingKeyword;
            String name = BaseActor.this.name;
            @Step("#keyword #name attempts to ")
            public void attemptsTo(Actor me) {
                performSteps("performAs", BaseActor.this, tasks);
            }
        })});
    }

    public void performSteps(String methodName, Object performer, Object... stepObjects) {
        performSteps(extractInfo(methodName, performer, stepObjects));
    }

    public void performSteps(StepMethodInfo[] steps) {
        StepErrorTally errorTally = new StepErrorTally(getStopWatch());
        for (StepMethodInfo stepMethodInfo : steps) {
            performStep(errorTally, stepMethodInfo);
        }
        errorTally.reportAnyErrors();
    }

    private void performStep(StepErrorTally errorTally, StepMethodInfo stepMethodInfo) {
        errorTally.startStopWatch();
        try {
            pushStepPath(stepMethodInfo);
            eventBus.broadcast(new StepEvent(this, stepMethodInfo, StepEventType.STARTED));
            if (errorTally.shouldSkip() || stepMethodInfo.isSkipped()) {
                eventBus.broadcast(new StepEvent(this, stepMethodInfo, StepEventType.SKIPPED, errorTally.stopStopWatch()));
            } else if (stepMethodInfo.isPending()) {
                errorTally.setPending();
                eventBus.broadcast(new StepEvent(this, stepMethodInfo, StepEventType.PENDING, errorTally.stopStopWatch()));
            } else {
                stepMethodInfo.invoke();
                eventBus.broadcast(new StepEvent(this, stepMethodInfo, StepEventType.SUCCESSFUL, errorTally.stopStopWatch()));
            }
        } catch (Throwable t) {
            errorTally.addThrowable(t);
            if (errorTally.indicatesPending(t)) {
                eventBus.broadcast(new StepEvent(this, stepMethodInfo, StepEventType.PENDING, errorTally.stopStopWatch(), t));
            } else if (errorTally.indicatesAssertionFailed(t)) {
                eventBus.broadcast(new StepEvent(this, stepMethodInfo, StepEventType.ASSERTION_FAILED, errorTally.stopStopWatch(), t));
            } else {
                eventBus.broadcast(new StepEvent(this, stepMethodInfo, StepEventType.FAILED, errorTally.stopStopWatch(), t));
            }
        } finally {
            popStepPath();
        }
    }

    private void popStepPath() {
        stepPaths.pollLast();
    }

    private void pushStepPath(StepMethodInfo stepMethodInfo) {
        String stepId = NameConverter.filesystemSafe(stepMethodInfo.getName());
        if (stepPaths.isEmpty()) {
            stepPaths.offerLast(stepId);
        } else {
            stepPaths.offerLast(stepPaths.getLast() + "/" + stepId);
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
        doSomething.asActor(this);
        abilities.put(doSomething.getClass(), doSomething);
    }

    @Override
    public <T extends Ability> T usingAbilityTo(Class<? extends T> doSomething) {
        return (T) abilities.get(doSomething);
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
