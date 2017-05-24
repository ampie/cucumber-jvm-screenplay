package cucumber.scoping;


import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.Memory;
import cucumber.screenplay.internal.BaseActorOnStage;

public abstract class UserInScope extends BaseActorOnStage implements Memory, ActorOnStage {

    public UserInScope(UserTrackingScope scope, Actor actor) {
        super(scope, actor);
    }

    public final String getName() {
        return getActor().getName();
    }
    

    public String getScopePath() {
        return this.getScope().getScopePath() + "/" + getId();
    }

    
    @Override
    public <T> T recall(String name) {
        T value = getScope().recallForUser(id, name);
        if (value == null) {
            value = getActor().recall(name);
        }
        if (value == null) {
            value = getScope().recallForUser(getScope().getEverybodyScope().getId(), name);
        }
        return value;
    }

    @Override
    public <T> T recall(Class<T> clzz) {
        return recall(clzz.getName());
    }


    public UserTrackingScope getScope() {
        return (UserTrackingScope) getScene();
    }


}
