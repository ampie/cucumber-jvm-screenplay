package com.sbg.bdd.screenplay.scoped;

import com.sbg.bdd.screenplay.core.annotations.ActorListener;
import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.ActorEvent;
import com.sbg.bdd.screenplay.core.events.SceneEvent;
import com.sbg.bdd.screenplay.core.events.StepEvent;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;


public class EventStore {
    static private List<EventObject> events = new ArrayList<>();

    @SceneListener()
    public void onScope(SceneEvent event) {
        events.add(event);
    }

    @ActorListener()
    public void onScope(ActorEvent event) {
        events.add(event);
    }

    @StepListener
    public void onStep(StepEvent event){
        events.add(event);
    }
    public static List<EventObject> getEvents() {
        return events;
    }
}
