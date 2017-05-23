package cucumber.scoping;


import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;

import java.util.HashMap;
import java.util.Map;

import static cucumber.scoping.IdGenerator.fromName;

public abstract class UserTrackingScope extends VerificationScope {
    private Map<String, UserInScope> usersInScope = new HashMap<>();
    private String idOfUserInSpotlight = null;

    public UserTrackingScope(VerificationScope containingScope, String name) {
        super(containingScope, name);
    }

    @Override
    public UserTrackingScope getContainingScope() {
        return (UserTrackingScope) super.getContainingScope();
    }

    public <T extends ActorOnStage> T shineSpotlightOn(Actor actor) {
        UserInScope t = enter(actor);
        getUserInSpotlight().exitSpotlight();
        t.enterSpotlight();
        this.idOfUserInSpotlight = t.getId();
        return (T) t;
    }

    @Override
    protected void completeChildren() {
        super.completeChildren();
        getUserInSpotlight().exitSpotlight();
        for (UserInScope userInScope : usersInScope.values()) {
            userInScope.exitStage();
        }
    }

    public <T extends UserInScope> T enter(Actor actor) {
        UserInScope t = usersInScope.get(fromName(actor.getName()));
        if (t == null) {
            if (EverybodyInScope.isEverybody(actor)) {
                t = new EverybodyInScope(this, actor);
            } else if (GuestInScope.isGuest(actor)) {
                t = new GuestInScope(this, actor);
            } else {
                t = new ActorInScope(this, actor);
            }
            usersInScope.put(t.getId(), t);
            t.enterStage();
        }
        return (T) t;
    }

    public UserInScope getUserInSpotlight() {
        if (idOfUserInSpotlight != null) {
            return getUsersInScope().get(idOfUserInSpotlight);
        } else {
            return enter(EverybodyInScope.everybody(getGlobalScope()));
        }
    }

    public <T> T recallForUser(String userScopeId, String variableName) {
        UserInScope userInScope = usersInScope.get(userScopeId);
        Object result = null;
        if (userInScope != null) {
            result = userInScope.recallImmediately(variableName);
        }
        if (result == null && getContainingScope() != null) {
            result = getContainingScope().recallForUser(userScopeId, variableName);
        }
        return (T) result;
    }


    public EverybodyInScope getEverybodyScope() {
        Actor everybody = EverybodyInScope.everybody(this.getGlobalScope());
        if(!usersInScope.containsKey(everybody.getName())){
            usersInScope.put(everybody.getName(), new EverybodyInScope(this, everybody));
        }
        return (EverybodyInScope) usersInScope.get(everybody.getName());
    }


    public Map<String, UserInScope> getUsersInScope() {
        return usersInScope;
    }

    public void exit(Actor actor) {
        UserInScope userInScope = getUsersInScope().get(fromName(actor.getName()));
        if (userInScope != null) {
            userInScope.exitStage();
            usersInScope.remove(userInScope.getId());
        }

    }
}
