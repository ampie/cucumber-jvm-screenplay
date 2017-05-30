package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.annotations.Subject;

/**
 * Created by ampie on 2017/05/24.
 */
public class IDiedAfterASecond implements Question<Boolean> {
    long start =-1;

    @Override
    @Subject("I died after a second")
    public Boolean answeredBy(Actor actor) {
        if(start == -1){
            start = System.currentTimeMillis();
        }
        boolean b = System.currentTimeMillis() > start + 1000;
        return b;
    }
}
