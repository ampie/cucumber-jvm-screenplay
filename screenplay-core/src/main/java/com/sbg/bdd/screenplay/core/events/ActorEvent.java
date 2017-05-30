package com.sbg.bdd.screenplay.core.events;


import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement;

import java.util.EventObject;


public class ActorEvent extends EventObject {
    private final ActorInvolvement involvement;

    public ActorEvent(ActorOnStage source, ActorInvolvement involvement) {
        super(source);
        this.involvement=involvement;

    }
    public ActorOnStage getActorOnStage(){
        return (ActorOnStage) getSource();
    }

    public ActorInvolvement getInvolvement() {
        return involvement;
    }
}
