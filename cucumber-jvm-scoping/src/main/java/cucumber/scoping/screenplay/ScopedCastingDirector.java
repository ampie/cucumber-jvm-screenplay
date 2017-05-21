package cucumber.scoping.screenplay;

import cucumber.scoping.IdGenerator;
import cucumber.scoping.persona.PersonaClient;
import cucumber.screenplay.Actor;
import cucumber.screenplay.actors.CastingDirector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ScopedCastingDirector implements CastingDirector {
    private PersonaClient personaClient;
    private Path resourceRoot;

    public ScopedCastingDirector(PersonaClient personaClient, Path resourceRoot) {
        this.personaClient = personaClient;
        this.resourceRoot = resourceRoot;
    }

    @Override
    public Actor recruitActor(String name) {
        try {
            return new ScopedActor(name, personaClient.preparePersona(getPersonaFile(name)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private File getPersonaFile(String name) {
        return resourceRoot.resolveSibling(IdGenerator.fromName(name)).resolve("persona.json").toFile();
    }

    @Override
    public Actor interviewActor(String name) {
        try {
            return new ScopedActor(name, personaClient.installPersona(getPersonaFile(name)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
