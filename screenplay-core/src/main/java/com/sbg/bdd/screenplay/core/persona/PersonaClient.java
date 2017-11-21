package com.sbg.bdd.screenplay.core.persona;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.WritableResource;

import java.util.Map;


public interface PersonaClient<DATAOBJECT> {
    String PERSONA_CLIENT="persona.client";
    /**
     *
     * @return the name of the text file in the persona's root directory that will be used to create the persona from
     */
    String getDefaultPersonaFileName();


    /**
     * Ensures that the given persona is installed in the target environment as specified by the state of the personaFile
     * If the persona is already in the target environment, and it is not a static persona, this will first
     * delete the persona and then install the persona in the target environment to reflect the state of the personaFile
     */
    Persona<DATAOBJECT> ensurePersonaInState(String name, ReadableResource personaFile);

    void establishSessionFor(Persona<?> persona, Map<String, Object> loginContext);

    /**
     * Extracts the persona from the target environment, typically to save it to a local file during development
     * @param name
     * @param username
     * @return
     */
    Persona<DATAOBJECT> extractPersona(String name, String username);


}
