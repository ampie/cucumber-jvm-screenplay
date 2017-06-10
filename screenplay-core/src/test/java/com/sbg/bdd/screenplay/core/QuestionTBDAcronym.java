package com.sbg.bdd.screenplay.core;


public class QuestionTBDAcronym implements Question<Boolean> {
    @Override
    public Boolean answeredBy(Actor actor) {
        return true;
    }
}
