package com.sbg.bdd.screenplay.core.internal;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.actors.CastingDirector;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.screenplay.core.util.NameConverter;

public class BaseCastingDirector implements CastingDirector {
    private ScreenPlayEventBus eventBus;
    private PersonaClient personaClient;
    private ResourceContainer resourceRoot;

    public BaseCastingDirector(ScreenPlayEventBus eventBus, PersonaClient personaClient, ResourceContainer resourceRoot) {
        this.eventBus = eventBus;
        this.personaClient = personaClient;
        this.resourceRoot = resourceRoot;
    }

    @Override
    public Actor recruitActor(String name) {
        BaseActor result = new BaseActor(eventBus, name);
        if (hasPersona(name)) {
            result.remember(Actor.PERSONA, personaClient.preparePersona(name, getPersonaResource(name)));
        }
        return result;
    }

    private boolean hasPersona(String name) {
        return !(name.equals(Actor.EVERYBODY) || name.equals(Actor.GUEST));
    }

    private ReadableResource getPersonaResource(String name) {
        return (ReadableResource) resourceRoot.resolveExisting(NameConverter.filesystemSafe(name), personaClient.getDefaultPersonaFileName());
    }

    @Override
    public Actor findCandidate(String name) {
        BaseActor result = new BaseActor(eventBus, name);
        if (hasPersona(name)) {
            result.remember(Actor.PERSONA, personaClient.installPersona(name, getPersonaResource(name)));
        }
        return result;
    }
}
