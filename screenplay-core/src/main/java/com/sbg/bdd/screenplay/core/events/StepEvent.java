package com.sbg.bdd.screenplay.core.events;

import com.sbg.bdd.screenplay.core.annotations.StepEventType;
import com.sbg.bdd.screenplay.core.internal.StepMethodInfo;

import java.util.EventObject;

public class StepEvent extends EventObject {
    private final StepEventType type;
    private final long duration;
    private final Throwable error;

    public StepEvent(StepMethodInfo source, StepEventType type) {
        this(source, type, -1);
    }

    public StepEvent(StepMethodInfo source, StepEventType type, long duration) {
        this(source, type, duration, null);
    }

    public StepEvent(StepMethodInfo source, StepEventType type, long duration, Throwable error) {
        super(source);
        this.type = type;
        this.duration = duration;
        this.error = error;
    }

    public StepMethodInfo getInfo() {
        return (StepMethodInfo) getSource();
    }

    public long getDuration() {
        return duration;
    }

    public Throwable getError() {
        return error;
    }

    public StepEventType getType() {
        return type;
    }

    public String getStepPath() {
        return getInfo().getStepPath();
    }
    public int getStepLevel() {
        return getInfo().getStepLevel();
    }
}
