package cucumber.scoping.persona.local;


import cucumber.scoping.persona.CharacterType;
import cucumber.scoping.persona.Persona;
import gherkin.deps.com.google.gson.JsonObject;

public class LocalPersona implements Persona<JsonObject>{
    private final JsonObject data;
    private String name;
    private CharacterType characterType;

    public LocalPersona(String name,JsonObject data) {
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
}
