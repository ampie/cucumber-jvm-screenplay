package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.annotations.Step;

public class RememberAction implements DownstreamStub {
    private String name;
    private Object value;

    public RememberAction(String name) {
        this.name = name;
    }
    public RememberAction as(Object value){
        this.value=value;
        return this;
    }
    public RememberAction is(Object value){
        return as(value);
    }
    public RememberAction toBe(Object value){
        return as(value);
    }

    @Override
    @Step("the variable #name to be #value")
    public void performOnStage(ActorOnStage actorOnStage) {
        actorOnStage.remember(name,value);
    }
    public static RememberAction rememberThat(String name){
        return new RememberAction(name);
    }
    public static RememberAction remember(String name){
        return rememberThat(name);
    }
    public static RememberAction theVariable(String name){
        return rememberThat(name);
    }
}
