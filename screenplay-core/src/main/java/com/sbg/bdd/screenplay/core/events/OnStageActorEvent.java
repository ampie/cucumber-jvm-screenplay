package com.sbg.bdd.screenplay.core.events;


import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement;

import java.util.EventObject;


public class OnStageActorEvent extends EventObject {
    private ActorInvolvement involvement;

    public OnStageActorEvent(ActorOnStage source, ActorInvolvement involvement) {
        super(source);
        this.involvement = involvement;
    }

    public ActorOnStage getActorOnStage() {
        return (ActorOnStage) getSource();
    }

    public Actor getActor() {
        return (Actor) getSource();
    }

    public ActorInvolvement getInvolvement() {
        return involvement;
    }
}
