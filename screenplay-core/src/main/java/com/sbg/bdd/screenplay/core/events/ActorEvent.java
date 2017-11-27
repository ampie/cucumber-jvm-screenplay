package com.sbg.bdd.screenplay.core.events;


import com.sbg.bdd.screenplay.core.Ability;
import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.annotations.ActorEventType;
import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement;
import com.sbg.bdd.screenplay.core.persona.Persona;

import java.util.EventObject;


public class ActorEvent extends EventObject {
    private Persona<?> persona;
    private Ability ability;
    private ActorEventType eventType;


    public ActorEvent(Actor source, ActorEventType eventType) {
        super(source);
        this.eventType = eventType;
    }
    public ActorEvent(Actor source, ActorEventType eventType, Ability ability) {
        this(source,eventType);
        this.ability = ability;
    }

    public ActorEvent(Actor source, ActorEventType eventType, Persona<?> persona) {
        this(source,eventType);
        this.persona = persona;
    }
    public Ability getAbility() {
        return ability;
    }


    public Actor getActor() {
        return (Actor) getSource();
    }

    public ActorEventType getEventType() {
        return eventType;
    }

    public Persona<?> getPersona() {
        return persona;
    }

    public void setPersona(Persona<?> persona) {
        this.persona = persona;
    }
}
