package com.sbg.bdd.screenplay.core.internal;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.Ability;
import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.actors.Cast;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.screenplay.core.util.NameConverter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PersonaBasedCast implements Cast {
    private final ScreenPlayEventBus screenPlayEventBus;
    private final PersonaClient personaClient;
    private final ResourceContainer personaRoot;

    Map<String, Actor> actors = new HashMap<>();

    public PersonaBasedCast(ScreenPlayEventBus screenPlayEventBus, PersonaClient personaClient, ResourceContainer personaRoot) {

        this.screenPlayEventBus = screenPlayEventBus;
        this.personaClient = personaClient;
        this.personaRoot = personaRoot;
    }


    private boolean hasPersona(String name) {
        return personaClient!=null && personaRoot !=null && !(name.equals(Actor.EVERYBODY) || name.equals(Actor.GUEST));
    }

    private ReadableResource getPersonaResource(String name) {
        return (ReadableResource) personaRoot.resolveExisting(NameConverter.filesystemSafe(name), personaClient.getDefaultPersonaFileName());
    }

    @Override
    public Actor actorNamed(String actorName, Ability... abilities) {
        Actor result = actors.get(actorName);
        if (result == null) {
            result = new BaseActor(screenPlayEventBus, actorName);
            if (hasPersona(actorName)) {
                result.remember(Actor.PERSONA, personaClient.ensurePersonaInState(actorName, getPersonaResource(actorName)));
                result.remember(PersonaClient.PERSONA_CLIENT, personaClient);
            }
            actors.put(actorName, result);
            for (Ability ability : abilities) {
                result.can(ability);
            }
        }
        return result;
    }

    @Override
    public Collection<Actor> getActors() {
        return actors.values();
    }

    @Override
    public void dismiss(Actor actor) {
        actors.remove(actor.getName());
    }
}
