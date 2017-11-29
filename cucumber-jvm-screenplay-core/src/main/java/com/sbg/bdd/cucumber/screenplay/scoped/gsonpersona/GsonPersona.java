package com.sbg.bdd.cucumber.screenplay.scoped.gsonpersona;


import com.sbg.bdd.screenplay.core.persona.CharacterType;
import com.sbg.bdd.screenplay.core.persona.Persona;
import gherkin.deps.com.google.gson.JsonObject;

public class GsonPersona implements Persona<JsonObject> {
    private final JsonObject data;
    private String name;
    private CharacterType characterType;

    public GsonPersona(String name, JsonObject data) {
        this.name = name;
        this.data=data;
    }

    @Override
    public String getUserName() {
        return data.get("userName").getAsString();
    }

    @Override
    public String getSessionToken() {
        return name+"sessionId";
    }

    @Override
    public JsonObject getDataObject() {
        return data;
    }

    @Override
    public CharacterType getCharacterType() {
        return this.characterType;
    }

    @Override
    public void setCharacterType(CharacterType type) {
        this.characterType=type;
    }

    @Override
    public String getUrl() {
        return data.has("personaUrl")?data.get("personaUrl").getAsString():null;
    }

}
