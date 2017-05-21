package cucumber.scoping.persona;

public interface Persona <JSONOBJECT>{
    String getUserName();
    String getSessionToken();
    JSONOBJECT getJsonObject();
    CharacterType getCharacterType();
    void setCharacterType(CharacterType type);
}
