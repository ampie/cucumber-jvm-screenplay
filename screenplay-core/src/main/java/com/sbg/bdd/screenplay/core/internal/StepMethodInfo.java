package com.sbg.bdd.screenplay.core.internal;

public interface StepMethodInfo {
    String getStepPath();

    String getKeyword();

    String getNameExpression();

    String getName();

    boolean isPending();

    boolean isSkipped();

    String getLocation();

    int getStepLevel();
}
