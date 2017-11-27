package com.sbg.bdd.screenplay.core.events;


import com.sbg.bdd.screenplay.core.Ability;
import com.sbg.bdd.screenplay.core.annotations.ActorListener;
import com.sbg.bdd.screenplay.core.persona.Persona;

import java.lang.reflect.Method;

import static com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus.mostSpecific;


public class ActorEventCallback extends ScreenPlayEventCallback {
    Class<? > personaType;
    Class<?> abilityType;

    public ActorEventCallback(Object target, Method method, ActorListener b) {
        this(target, method, b.personaType(), b.abilityType(), b.actorNamePattern());
    }

    public ActorEventCallback(Object target, Method method, Class<? extends Persona> personaType, Class<? extends Ability> abilityType, String namePattern) {
        super(target, method, namePattern, 0);
        this.personaType = mostSpecific(method, personaType);
        this.abilityType = mostSpecific(method, abilityType);
    }


    public boolean isMatch(ActorEvent event) {

        return personaTypeMatches(event) && abilityTypeMatches(event) &&
                namesMatch(event.getActor().getName());
    }

    private boolean abilityTypeMatches(ActorEvent event) {
        return event.getAbility()==null || abilityType.isInstance(event.getAbility());
    }

    private boolean personaTypeMatches(ActorEvent event) {
        return event.getPersona() == null || personaType.isInstance(event.getPersona());
    }


}
