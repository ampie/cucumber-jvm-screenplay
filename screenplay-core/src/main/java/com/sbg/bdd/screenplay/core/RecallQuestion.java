package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.actors.OnStage;

public class RecallQuestion<T> implements Question<T> {
    private String variableName;

    public RecallQuestion(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public T answeredBy(Actor actor) {
        return OnStage.callActorToStage(actor).recall(variableName);
    }

    public static <U> RecallQuestion<U> recallThat(String variableName){
        return new RecallQuestion<U>(variableName);
    }
}
