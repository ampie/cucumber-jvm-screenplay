package com.sbg.bdd.screenplay.core.persona.properties;


import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.WritableResource;
import com.sbg.bdd.screenplay.core.persona.Persona;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class PropertiesPersonaClient implements PersonaClient<Properties> {


    @Override
    public Persona extractPersona(String name, String userName) {
        Properties data = new Properties();
        data.setProperty("name", name);
        data.setProperty("userName", userName);
        return new PropertiesPersona(name, data);
    }

    @Override
    public String getDefaultPersonaFileName() {
        return "persona.properties";
    }

    @Override
    public Persona<Properties> ensurePersonaInState(String name, ReadableResource personaFile) {
        return readPersona(name, personaFile);
    }

    @Override
    public void establishSessionFor(Persona<?> persona, Map<String, Object> loginContext) {

    }
    private Persona readPersona(String name, ReadableResource file) {
        try {
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(file.read()));
            return new PropertiesPersona(name, properties);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
