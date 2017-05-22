package cucumber.scoping.persona;

import java.io.File;
import java.io.IOException;

public interface PersonaClient<JSONOBJECT> {
    /**
     * Prepares the given persona for testing in the deployed environment.
     */

    Persona<JSONOBJECT> preparePersona(String name, File personaFile) throws IOException;
    
    void deletePersona(String username) throws IOException;
    
    Persona<JSONOBJECT> installPersona(String name, File file) throws IOException;

    void savePersonaLocally(Persona<JSONOBJECT> persona, File targetFile) throws IOException;

    Persona<JSONOBJECT> extractPersona(String name,  String username) throws IOException;
    
}
