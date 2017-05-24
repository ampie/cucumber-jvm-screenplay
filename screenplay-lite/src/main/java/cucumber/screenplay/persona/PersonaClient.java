package cucumber.screenplay.persona;

import java.io.File;
import java.io.IOException;

public interface PersonaClient<DATAOBJECT> {
    /**
     * Prepares the given persona for testing in the deployed environment.
     */

    Persona<DATAOBJECT> preparePersona(String name, File personaFile) throws IOException;
    
    void deletePersona(String username) throws IOException;
    
    Persona<DATAOBJECT> installPersona(String name, File file) throws IOException;

    void savePersonaLocally(Persona<DATAOBJECT> persona, File targetFile) throws IOException;

    Persona<DATAOBJECT> extractPersona(String name,  String username) throws IOException;
    
    String getDefaultPersonaFileName();
    
}
