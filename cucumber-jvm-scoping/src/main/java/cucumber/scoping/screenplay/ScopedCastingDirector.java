package cucumber.scoping.screenplay;

import cucumber.scoping.IdGenerator;
import cucumber.scoping.persona.PersonaClient;
import cucumber.screenplay.Actor;
import cucumber.screenplay.actors.CastingDirector;
import cucumber.screenplay.formatter.CucumberChildStepInfo;

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
            if(hasNoPersona(name)){
                return new ScopedActor(name,null);
            }
            return new ScopedActor(name, personaClient.preparePersona(name, getPersonaFile(name)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean hasNoPersona(String name) {
        return name.equals("everybody") || name.equals("guest");
    }

    private File getPersonaFile(String name) {
        return resourceRoot.resolve(IdGenerator.fromName(name)).resolve("persona.json").toFile();
    }

    @Override
    public ScopedActor interviewActor(String name) {
        try {
            if(hasNoPersona(name)){
                return new ScopedActor(name,null);
            }
            return new ScopedActor(name, personaClient.installPersona(name, getPersonaFile(name)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
