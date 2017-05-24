package cucumber.screenplay.internal;

import cucumber.screenplay.Actor;
import cucumber.screenplay.actors.CastingDirector;
import cucumber.screenplay.events.ScreenPlayEventBus;
import cucumber.screenplay.persona.PersonaClient;
import cucumber.screenplay.util.NameConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class BaseCastingDirector implements CastingDirector {
    private ScreenPlayEventBus eventBus;
    private PersonaClient personaClient;
    private Path resourceRoot;

    public BaseCastingDirector(ScreenPlayEventBus eventBus, PersonaClient personaClient, Path resourceRoot) {
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
        return resourceRoot.resolve(NameConverter.filesystemSafe(name)).resolve(personaClient.getDefaultPersonaFileName()).toFile();
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
