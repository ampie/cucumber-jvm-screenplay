package cucumber.screenplay;

import cucumber.runtime.StopWatch;


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
