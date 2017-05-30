package com.sbg.bdd.screenplay.core;

public interface Question<ANSWER> {
    ANSWER answeredBy(Actor actor);
}
