package com.sbg.bdd.screenplay.core.persona;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.WritableResource;

import java.util.Map;


public interface PersonaClient<DATAOBJECT> {
    /**
     * Prepares the given persona for testing in the deployed environment.
     */

    Persona<DATAOBJECT> preparePersona(String name, ReadableResource personaFile);

    /**
     * Prepares the given persona for testing in the deployed environment.
     */

    Persona<DATAOBJECT> preparePersona(String name, ReadableResource personaFile, Map<String, Object> loginContext);

    void deletePersona(String username);
    
    Persona<DATAOBJECT> installPersona(String name, ReadableResource file);

    void savePersonaLocally(Persona<DATAOBJECT> persona, WritableResource targetFile);

    Persona<DATAOBJECT> extractPersona(String name, String username);
    
    String getDefaultPersonaFileName();
    
}
