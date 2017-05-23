package cucumber.scoping;

import cucumber.scoping.IdGenerator;
import cucumber.scoping.events.ScopeEventBus;
import cucumber.scoping.persona.PersonaClient;
import cucumber.screenplay.Actor;
import cucumber.screenplay.actors.CastingDirector;
import cucumber.screenplay.internal.BaseActor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ScopedCastingDirector implements CastingDirector {
    private ScopeEventBus eventBus;
    private PersonaClient personaClient;
    private Path resourceRoot;

    public ScopedCastingDirector(ScopeEventBus eventBus, PersonaClient personaClient, Path resourceRoot) {
        this.eventBus = eventBus;
        this.personaClient = personaClient;
        this.resourceRoot = resourceRoot;
    }

    @Override
    public Actor recruitActor(String name) {
        try {
            BaseActor result = new BaseActor(eventBus, name);
            if (hasPersona(name)) {
                result.remember("persona", personaClient.preparePersona(name, getPersonaFile(name)));
            }
            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean hasPersona(String name) {
        return !(name.equals("everybody") || name.equals("guest"));
    }

    private File getPersonaFile(String name) {
        return resourceRoot.resolve(IdGenerator.fromName(name)).resolve(personaClient.getDefaultPersonaFileName()).toFile();
    }

    @Override
    public Actor findCandidate(String name) {
        try {
            BaseActor result = new BaseActor(eventBus, name);
            if (hasPersona(name)) {
                result.remember("persona", personaClient.installPersona(name, getPersonaFile(name)));
            }
            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }}
