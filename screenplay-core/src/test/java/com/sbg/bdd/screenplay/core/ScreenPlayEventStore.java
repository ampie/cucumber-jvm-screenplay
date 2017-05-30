package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.StepEvent;

import java.util.ArrayList;
import java.util.List;

public class ScreenPlayEventStore {
    private static  List<StepEvent> events = new ArrayList<>();
    @StepListener()
    public void listen(StepEvent event){
        events.add(event);
    }
    public static List<StepEvent> getEvents() {
        return events;
    }
}
