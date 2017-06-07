package com.sbg.bdd.screenplay.wiremock;

import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.StepEvent;

import java.util.ArrayList;
import java.util.List;

public class Listener {
    public static List<StepEvent> EVENTS = new ArrayList<>();

    @StepListener
    public void listen(StepEvent event) {
        EVENTS.add(event);

    }
}