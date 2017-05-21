package cucumber.scoping.persona;

import java.io.File;
import java.io.IOException;

public interface PersonaClient {
    /**
     * Prepares the given persona for testing in the deployed environment.
     * @param personaName which will be used as a path relative to the resource root
     * @return A JsonObject representing the persona in question, already logged in
     * @throws IOException
     */
    Persona preparePersona(String personaName) throws IOException;
    Persona preparePersona(File personaFile) throws IOException;
    
    void deletePersona(String username) throws IOException;
    
    Persona installPersona(File file) throws IOException;
    
    Persona installPersona(Persona toClone) throws IOException;
    
    Persona extractPersona(String username) throws IOException;
    
    void extractPersonaTo(String username,File destinationFile) throws IOException;
}
