package com.sbg.bdd.screenplay.scoped;

import com.sbg.bdd.screenplay.core.annotations.ActorInvolvementListener;
import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.OnStageActorEvent;
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

    @ActorInvolvementListener()
    public void onScope(OnStageActorEvent event) {
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
