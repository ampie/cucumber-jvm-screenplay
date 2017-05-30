package com.sbg.bdd.screenplay.core;

public class IDidNotDieError extends AssertionError{
    public IDidNotDieError(String message) {
        super(message);
    }

    public IDidNotDieError(Object detailMessage) {
        super(detailMessage);
    }
    public IDidNotDieError(String message, Throwable cause) {
        super(message, cause);
    }
}
