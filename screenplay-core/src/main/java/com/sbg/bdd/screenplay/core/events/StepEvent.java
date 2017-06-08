package com.sbg.bdd.screenplay.core.events;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.annotations.StepEventType;
import com.sbg.bdd.screenplay.core.internal.StepMethodInfo;

import java.util.EventObject;

public class StepEvent extends EventObject {
    private final StepEventType type;
    private final long duration;
    private final Throwable error;
    private final Actor actor;

    public StepEvent(Actor actor, StepMethodInfo source, StepEventType type) {
        this(actor,source, type, -1);
    }

    public StepEvent(Actor actor, StepMethodInfo source, StepEventType type, long duration) {
        this(actor, source, type, duration, null);
    }

    public StepEvent(Actor actor,StepMethodInfo source, StepEventType type, long duration, Throwable error) {
        super(source);
        this.actor=actor;
        this.type = type;
        this.duration = duration;
        this.error = error;
    }

    public Actor getActor() {
        return actor;
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
