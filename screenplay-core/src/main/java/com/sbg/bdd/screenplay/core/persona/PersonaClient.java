package com.sbg.bdd.screenplay.core.persona;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.WritableResource;

import java.io.File;
import java.io.IOException;

public interface PersonaClient<DATAOBJECT> {
    /**
     * Prepares the given persona for testing in the deployed environment.
     */

    Persona<DATAOBJECT> preparePersona(String name, ReadableResource personaFile) ;
    
    void deletePersona(String username);
    
    Persona<DATAOBJECT> installPersona(String name, ReadableResource file);

    void savePersonaLocally(Persona<DATAOBJECT> persona, WritableResource targetFile);

    Persona<DATAOBJECT> extractPersona(String name,  String username);
    
    String getDefaultPersonaFileName();
    
}
