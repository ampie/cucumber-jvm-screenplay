package com.sbg.bdd.screenplay.core;


public class MyDiagnosticError extends AssertionError{
    public MyDiagnosticError(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDiagnosticError(String detailMessage) {
        super(detailMessage);
    }

    public MyDiagnosticError() {
    }
}
