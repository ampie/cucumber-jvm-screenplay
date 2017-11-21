package com.sbg.bdd.cucumber.screenplay.scoped.gsonpersona;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.WritableResource;
import com.sbg.bdd.screenplay.core.persona.Persona;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class GsonPersonaClient implements PersonaClient<JsonObject> {


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

    @Override
    public Persona<JsonObject> ensurePersonaInState(String name, ReadableResource personaFile) {
        return readPersona(name, personaFile);
    }

    @Override
    public void establishSessionFor(Persona<?> persona, Map<String, Object> loginContext) {

    }
    private Persona readPersona(String name, ReadableResource file) {
        JsonParser parser = new JsonParser();
        JsonObject element = (JsonObject) parser.parse(new InputStreamReader(new ByteArrayInputStream(file.read())));
        return new GsonPersona(name, element);
    }

}
