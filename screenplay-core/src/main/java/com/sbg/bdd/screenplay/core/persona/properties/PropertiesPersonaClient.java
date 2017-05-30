package com.sbg.bdd.screenplay.core.persona.properties;


import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.WritableResource;
import com.sbg.bdd.screenplay.core.persona.Persona;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesPersonaClient implements PersonaClient<Properties> {

    @Override
    public Persona preparePersona(String personaName, ReadableResource file) {
        return readPersona(personaName, file);
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

    @Override
    public void deletePersona(String username) {

    }

    @Override
    public Persona installPersona(String name, ReadableResource file) {
        return readPersona(file.getName(), file);
    }

    @Override
    public void savePersonaLocally(Persona<Properties> persona, WritableResource targetFile) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            persona.getDataObject().store(out, "saved");
            targetFile.write(out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

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

}
