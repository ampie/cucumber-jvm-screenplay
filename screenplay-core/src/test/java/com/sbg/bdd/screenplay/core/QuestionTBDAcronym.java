package com.sbg.bdd.screenplay.core;

/**
 * Created by ampie on 2017/05/24.
 */
public class QuestionTBDAcronym implements Question<Boolean> {
    @Override
    public Boolean answeredBy(Actor actor) {
        return true;
    }
}
