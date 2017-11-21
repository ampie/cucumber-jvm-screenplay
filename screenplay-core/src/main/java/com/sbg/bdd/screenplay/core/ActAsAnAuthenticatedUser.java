package com.sbg.bdd.screenplay.core;

import com.sbg.bdd.screenplay.core.persona.Persona;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;

import java.util.HashMap;

/**
 * Override this class if your application requires additional information to authenticate
 */
public class ActAsAnAuthenticatedUser implements Ability{
    private Actor actor;

    @Override
    public ActAsAnAuthenticatedUser asActor(Actor actor) {
        this.actor=actor;
        PersonaClient<?> personaClient =  actor.recall(PersonaClient.PERSONA_CLIENT);
        if(personaClient==null){
            throw new IllegalStateException("Actor '" + actor.getName() + "' cannot be authenticated");
        }
        personaClient.establishSessionFor(actor.getPersona(),new HashMap<String,Object>());
        return this;
    }
}
