package com.sbg.bdd.screenplay.core.persona.properties;

import com.sbg.bdd.screenplay.core.persona.CharacterType;
import com.sbg.bdd.screenplay.core.persona.Persona;

import java.util.Properties;

public class PropertiesPersona implements Persona<Properties> {
    private final Properties data;
    private String name;
    private CharacterType characterType;

    public PropertiesPersona(String name, Properties data) {
        this.name = name;
        this.data = data;
    }

    @Override
    public String getUserName() {
        return data.getProperty("userName");
    }

    @Override
    public String getSessionToken() {
        return name + "sessionId";
    }

    @Override
    public Properties getDataObject() {
        return data;
    }

    @Override
    public CharacterType getCharacterType() {
        return this.characterType;
    }

    @Override
    public void setCharacterType(CharacterType type) {
        this.characterType = type;
    }
}
