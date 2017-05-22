package cucumber.scoping;


import cucumber.scoping.annotations.UserInvolvement;
import cucumber.scoping.events.UserEvent;
import cucumber.screenplay.*;
import cucumber.screenplay.internal.BaseActorOnStage;
import cucumber.screenplay.internal.ChildStepInfo;
import cucumber.screenplay.internal.SimpleMemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cucumber.scoping.IdGenerator.fromName;

public abstract class UserInScope extends BaseActorOnStage implements Memory, ActorOnStage {
    private String id;
    private boolean active = false;
    private Map<String, UserInScope> usersInScope = new HashMap<>();
    private UserTrackingScope scope;
    private SimpleMemory memory = new SimpleMemory();
    private List<DownstreamVerification> downstreamExpectations = new ArrayList<>();

    public UserInScope(UserTrackingScope scope, Actor actor) {
        super(actor);
        this.scope = scope;
        this.id = fromName(actor.getName());
    }

    public final String getName() {
        return getActor().getName();
    }

    public String getId() {
        return this.id;
    }


    public String getScopePath() {
        return this.getScope().getScopePath() + "/" + getId();
    }

    @Override
    public void remember(String name, Object value) {
        memory.remember(name, value);
    }

    @Override
    public void remember(Object value) {
        memory.remember(value);
    }

    @Override
    public void forget(String name) {
        memory.forget(name);
    }

    @Override
    public <T> T recall(String name) {
        return getScope().recallForUser(id, name);
    }

    @Override
    public <T> T recall(Class<T> clzz) {
        return recall(clzz.getName());
    }

    public boolean isActive() {
        return this.active;
    }

    public void enter() {
        if (!isActive()) {
            getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.BEFORE_ENTER));
            enterWithoutEvents();
            getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.AFTER_ENTER));
        }
    }

    public void exit() {
        if (isActive()) {
            getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.BEFORE_EXIT));
            exitWithoutEvents();
            getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.AFTER_EXIT));
        }
    }

    protected void exitWithoutEvents() {
        memory.clear();
        this.active = false;
    }

    protected void enterWithoutEvents() {
        this.active = true;
    }

    public UserTrackingScope getScope() {
        return scope;
    }

    public <T> T recallImmediately(String name) {
        return memory.recall(name);
    }


}
