package cucumber.scoping;


import cucumber.screenplay.Actor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.Scene;
import cucumber.screenplay.internal.SpotlightOperator;
import cucumber.screenplay.actors.Performance;
import cucumber.screenplay.annotations.SceneEventType;
import cucumber.screenplay.events.SceneEvent;
import cucumber.screenplay.persona.CharacterType;
import cucumber.screenplay.persona.Persona;
import cucumber.screenplay.util.NameConverter;

import java.util.HashMap;
import java.util.Map;

public abstract class UserTrackingScope extends VerificationScope implements Scene {
    private Map<String, UserInScope> usersInScope = new HashMap<>();

    private SpotlightOperator spotlightOperator;

    public UserTrackingScope(UserTrackingScope containingScope, String name) {
        super(containingScope, name);
        spotlightOperator = new SpotlightOperator(this);

    }

    @Override
    public String getIdentifier() {
        return getScopePath();
    }

    @Override
    public Performance getPerformance() {
        return getGlobalScope();
    }

    @Override
    public UserTrackingScope getContainingScope() {
        return (UserTrackingScope) super.getContainingScope();
    }

    @Override
    public Map<String, ? extends ActorOnStage> getActorsOnStage() {
        return usersInScope;
    }

    @Override
    public ActorOnStage theActorInTheSpotlight() {
        if(spotlightOperator.getActorInTheSpotlight()==null && getActiveNestedScope() instanceof UserTrackingScope){
            return ((UserTrackingScope)getActiveNestedScope()).theActorInTheSpotlight();
        }
        return spotlightOperator.getActorInTheSpotlight();
    }

    @Override
    public UserInScope callActorToStage(Actor actor) {
        return enter(actor);
    }

    public ActorOnStage shineSpotlightOn(Actor actor) {
        return spotlightOperator.shineSpotlightOn(actor);
    }

    @Override
    public void dismissActorFromStage(Actor actor) {
        if (spotlightOperator.isSpotlightOn(actor)) {
            spotlightOperator.actorOutOfSpotlight();
        }
        dismissRecursively(actor);
        Persona<?> persona = actor.recall("persona");
        if (persona != null && persona.getCharacterType() == CharacterType.DYNAMIC) {
            //TODO do we want the parentScopes to remain in tact, but just reload the actor?
            //Data may have changed, needs to be reloaded
            getGlobalScope().getCast().dismiss(actor);
        }
    }

    @Override
    public <T> T recall(String variableName) {
        return getEverybodyScope().recall(variableName);
    }

    @Override
    public void remember(String variableName, Object value) {
        getEverybodyScope().remember(variableName, value);
    }

    @Override
    protected void completeChildren() {
        super.completeChildren();
        spotlightOperator.actorOutOfSpotlight();
        for (UserInScope userInScope : usersInScope.values()) {
            userInScope.exitStage();
        }
    }

    public <T extends UserInScope> T enter(Actor actor) {
        UserInScope t = usersInScope.get(NameConverter.filesystemSafe(actor.getName()));
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
        if (!usersInScope.containsKey(everybody.getName())) {
            usersInScope.put(everybody.getName(), new EverybodyInScope(this, everybody));
        }
        return (EverybodyInScope) usersInScope.get(everybody.getName());
    }

    public void start() {
        if (!isActive()) {
            //We only start once
            getEverybodyScope().remember("parentScene", getContainingScope());
            getGlobalScope().broadcast(new SceneEvent(this, SceneEventType.BEFORE_START));
            startWithoutEvents();
            getGlobalScope().broadcast(new SceneEvent(this, SceneEventType.AFTER_START));
        }
    }

    public final void complete() {
        if (isActive()) {
            //We only complete once
            completeChildren();
            getGlobalScope().broadcast(new SceneEvent(this, SceneEventType.BEFORE_COMPLETE));
            completeWithoutEvents();
            getGlobalScope().broadcast(new SceneEvent(this, SceneEventType.AFTER_COMPLETE));
        }
    }

    public Map<String, UserInScope> getUsersInScope() {
        return usersInScope;
    }

    private void dismissRecursively(Actor actor) {
        for (VerificationScope verificationScope : getNestedScopes()) {
            if (verificationScope instanceof UserTrackingScope) {
                ((UserTrackingScope) verificationScope).dismissRecursively(actor);
            }
        }
        UserInScope userInScope = getUsersInScope().get(NameConverter.filesystemSafe(actor.getName()));
        if (userInScope != null) {
            userInScope.exitStage();
            usersInScope.remove(userInScope.getId());
        }
    }
}
