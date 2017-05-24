package cucumber.screenplay.events;


import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.annotations.ActorInvolvement;

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
