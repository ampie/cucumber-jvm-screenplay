package cucumber.scoping;


import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.Memory;

import java.util.HashMap;
import java.util.Map;

import static cucumber.scoping.IdGenerator.fromName;

public abstract class UserTrackingScope extends VerificationScope implements Memory {
    private Map<String, UserInScope> usersInScope = new HashMap<>();
    private String currentUserInScopeId = null;

    public UserTrackingScope(VerificationScope containingScope, String name) {
        super(containingScope, name);
    }

    @Override
    protected void completeWithoutEvents() {
        for (UserInScope userInScope : usersInScope.values()) {
            userInScope.exit();
        }
        super.completeWithoutEvents();
    }

    @Override
    public UserTrackingScope getContainingScope() {
        return (UserTrackingScope)super.getContainingScope();
    }

    public <T extends ActorOnStage> T enter(Actor actor) {
        return (T) enter(actor, actor.getName());
    }

    private <T extends UserInScope> T enter(Actor actor, String actorName) {
        String id = fromName(actorName);
        UserInScope userInScope = usersInScope.get(id);
        if (userInScope == null) {
            Actor actorToUse = (Actor) (actor == null ? getGlobalScope().getCast().actorNamed(actorName) : actor);
            userInScope = new ActorInScope(this, actorToUse);
            usersInScope.put(id, userInScope);
            userInScope.enter();
        }
        return (T) userInScope;
    }

    public <T extends UserInScope> T enter(String name) {
        return enter(null, name);
    }

    public UserInScope getCurrentUserInScope() {
        if (currentUserInScopeId != null) {
            return getUsersInScope().get(currentUserInScopeId);
        } else {
            return enterEverybody();
        }
    }

    public <T> T recallForUser(String userScopeId, String variableName) {
        UserInScope userInScope = usersInScope.get(userScopeId);
        Object result = null;
        if (userInScope != null) {
            result = userInScope.recallImmediately(variableName);
        }
        if (result == null) {
            result = getEverybodyScope().recallImmediately(variableName);
        }
        if (result == null && getContainingScope() != null) {
            result =  getContainingScope().recallForUser(userScopeId, variableName);
        }
        return (T) result;
    }

    public GuestInScope getGuestScope() {
        return GuestInScope.from(this, usersInScope);
    }

    public EverybodyInScope getEverybodyScope() {
        return EverybodyInScope.from(this, usersInScope);
    }

    public GuestInScope enterGuest() {
        return (GuestInScope) enter(getGuestScope().getId());
    }

    public EverybodyInScope enterEverybody() {
        return (EverybodyInScope) enter(getEverybodyScope().getId());
    }

    @Override
    public void remember(String name, Object value) {
        getEverybodyScope().remember(name, value);
    }

    @Override
    public void remember(Object value) {
        getEverybodyScope().remember(value);
    }

    @Override
    public void forget(String name) {
        getEverybodyScope().forget(name);
    }

    @Override
    public <T> T recall(String name) {
        return getEverybodyScope().recall(name);
    }

    @Override
    public <T> T recall(Class<T> clzz) {
        return getEverybodyScope().recall(clzz);
    }


    public Map<String, UserInScope> getUsersInScope() {
        return usersInScope;
    }

}
