package com.sbg.bdd.cucumber.screenplay.scoped.gsonpersona;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.WritableResource;
import com.sbg.bdd.resource.file.WritableFileResource;
import com.sbg.bdd.screenplay.core.persona.Persona;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;
import gherkin.deps.com.google.gson.stream.JsonWriter;

import java.io.*;

public class GsonPersonaClient implements PersonaClient<JsonObject> {

    @Override
    public Persona preparePersona(String personaName, ReadableResource file) {
        return readPersona(personaName, file);
    }

    private Persona readPersona(String name, ReadableResource file) {
        JsonParser parser = new JsonParser();
        JsonObject element = (JsonObject) parser.parse(new InputStreamReader(new ByteArrayInputStream(file.read())));
        return new GsonPersona(name, element);
    }

    @Override
    public void deletePersona(String username) {
    }

    @Override
    public Persona installPersona(String name, ReadableResource file) {
        return readPersona(file.getName(), file);
    }

    @Override
    public void savePersonaLocally(Persona<JsonObject> persona, WritableResource targetFile) {

        targetFile.write(persona.getDataObject().toString().getBytes());
    }

    @Override
    public Persona extractPersona(String name, String userName) {
        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        data.addProperty("userName", userName);
        return new GsonPersona(name, data);
    }

    @Override
    public String getDefaultPersonaFileName() {
        return "persona.json";
    }

}
