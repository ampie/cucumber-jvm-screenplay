package com.sbg.bdd.screenplay.core;


public class PendingException extends ScreenPlayException {
    public PendingException(String message) {
        super(message);
    }

    public PendingException(String message, Throwable e) {
        super(message, e);
    }

    public PendingException(Throwable e) {
        super(e);
    }
}
