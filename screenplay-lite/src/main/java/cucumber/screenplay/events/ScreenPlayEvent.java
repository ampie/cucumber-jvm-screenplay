package cucumber.screenplay.events;

import cucumber.screenplay.internal.ChildStepInfo;

import java.util.EventObject;

public class ScreenPlayEvent extends EventObject {
    private final Type type;
    private final long duration;
    private final Throwable error;

    public static enum Type {
        STEP_STARTED, STEP_SUCCESSFUL, STEP_SKIPPED, STEP_FAILED, STEP_ASSERTION_FAILED, STEP_PENDING
    }

    public ScreenPlayEvent(ChildStepInfo source, Type type) {
        this(source,type,-1);
    }
    public ScreenPlayEvent(ChildStepInfo source, Type type, long duration) {
        this(source,type,duration, null);
    }
    public ScreenPlayEvent(ChildStepInfo source, Type type, long duration, Throwable error) {
        super(source);
        this.type=type;
        this.duration = duration;
        this.error = error;
    }

    public ChildStepInfo getInfo() {
        return (ChildStepInfo)getSource();
    }

    public long getDuration() {
        return duration;
    }

    public Throwable getError() {
        return error;
    }

    public Type getType() {
        return type;
    }
}
