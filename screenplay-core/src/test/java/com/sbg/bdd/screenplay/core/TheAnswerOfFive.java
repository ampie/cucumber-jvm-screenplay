package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.annotations.Subject;

@Subject("the answer")
public class TheAnswerOfFive implements Question<Integer> {

    @Override
    public Integer answeredBy(Actor actor) {
        return 5;
    }
}