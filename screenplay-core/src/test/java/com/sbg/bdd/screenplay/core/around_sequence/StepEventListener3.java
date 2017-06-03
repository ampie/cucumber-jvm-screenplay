package com.sbg.bdd.screenplay.core.around_sequence;

import com.sbg.bdd.screenplay.core.StepEventStore;
import com.sbg.bdd.screenplay.core.annotations.Around;
import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import org.apache.commons.lang3.tuple.MutablePair;

@Around(StepEventListener2.class)

public class StepEventListener3 {
    @StepListener()
    public void listenToAll(StepEvent event) {
        StepEventStore.EVENTS.add(new MutablePair<Class, StepEvent>(StepEventListener3.class, event));
    }
}
