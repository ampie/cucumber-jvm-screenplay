package com.sbg.bdd.screenplay.core.internal;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by ampie on 2017/05/24.
 */
public class StackedStopWatch implements StopWatch {
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
}
