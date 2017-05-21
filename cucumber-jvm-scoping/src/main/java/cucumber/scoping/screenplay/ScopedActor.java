package cucumber.scoping.screenplay;

import cucumber.scoping.persona.CharacterType;
import cucumber.scoping.persona.Persona;
import cucumber.screenplay.formatter.FormattingActor;


public class ScopedActor extends FormattingActor {
    private Persona<?> persona;
    private CharacterType characterType=CharacterType.STATIC;
    public ScopedActor(String name, Persona<?> persona) {
        super(name);
        this.persona = persona;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }

    @Override
    public <T> T recall(String name) {
        T result = currentRole.recall(name);
        if(result==null) {
            return super.recall(name);
        }else{
            return result;
        }
    }

    @Override
    public <T> T recall(Class<T> clzz) {
        return recall(clzz.getName());
    }
}
