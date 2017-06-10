package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.annotations.Step;

public class TaskFactory {
    public static Task performATask() {
        return new Task() {
            @Override
            @Step("perform a task")
            public <T extends Actor> T performAs(T actor) {
                return actor;
            }
        };
    }
    
    public static Task failATask() {
        return new Task() {
            @Override
            @Step("fail a task")
            public <T extends Actor> T performAs(T actor) {
                throw new IllegalArgumentException("arrrgh!");
            }
        };
    }
    
    public static Task pendingTask() {
        return new Task() {
            @Override
            @Step("pending task")
            public <T extends Actor> T performAs(T actor) {
                throw new PendingException("arrrgh!");
            }
        };
    }
    
    public static Task taskWithAssertionFailed() {
        return new Task() {
            @Override
            @Step("pending task")
            public <T extends Actor> T performAs(T actor) {
                throw new AssertionError("arrrgh!");
            }
        };
    }
}
