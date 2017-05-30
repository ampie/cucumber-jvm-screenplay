package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.annotations.Subject;

/**
 * Created by ampie on 2017/05/24.
 */
public class IDoExist implements Question<Boolean> {
    @Override
    @Subject("I do exist")
    public Boolean answeredBy(Actor actor) {
        return true;
    }
}
