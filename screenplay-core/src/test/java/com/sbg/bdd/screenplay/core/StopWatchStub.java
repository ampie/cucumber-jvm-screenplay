package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.internal.StopWatch;

public class StopWatchStub implements StopWatch {
    private long expectedDuration;

    public StopWatchStub(long expectedDuration) {
        this.expectedDuration = expectedDuration;
    }

    @Override
    public void start() {

    }

    @Override
    public long stop() {
        return expectedDuration;
    }
}
