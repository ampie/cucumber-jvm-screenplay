package com.sbg.bdd.screenplay.core.around_sequence;

import com.sbg.bdd.screenplay.core.StepEventStore;
import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import org.apache.commons.lang3.tuple.MutablePair;

public class StepEventListener1 {
    @StepListener()
    public void listenToAll(StepEvent event){
        StepEventStore.EVENTS.add(new MutablePair<Class, StepEvent>(StepEventListener1.class,event));
    }
}
