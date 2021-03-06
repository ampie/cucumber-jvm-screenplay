package com.sbg.bdd.screenplay.core.persona;

public interface Persona <JSONOBJECT>{
    String getUserName();
    String getSessionToken();
    JSONOBJECT getDataObject();
    CharacterType getCharacterType();
    void setCharacterType(CharacterType type);

    String getUrl();
}
