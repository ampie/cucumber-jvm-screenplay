package cucumber.scoping;


import cucumber.scoping.annotations.UserInvolvement;
import cucumber.scoping.events.UserEvent;
import cucumber.screenplay.*;
import cucumber.screenplay.internal.BaseActorOnStage;
import cucumber.screenplay.internal.SimpleMemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cucumber.scoping.IdGenerator.fromName;

public abstract class UserInScope extends BaseActorOnStage implements Memory, ActorOnStage {
    private String id;
    private boolean active = false;
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
        T value = getScope().recallForUser(id, name);
        if(value == null){
            value=getActor().recall(name);
        }
        if(value == null){
            value = getScope().recallForUser(getScope().getEverybodyScope().getId(),name);
        }
        return value;
    }

    @Override
    public <T> T recall(Class<T> clzz) {
        return recall(clzz.getName());
    }

    public boolean isActive() {
        return this.active;
    }

    public void enterSpotlight() {
        if (isActive()) {
            getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.INTO_SPOTLIGHT));
        }
    }

    public void exitSpotlight() {
        if (isActive()) {
            getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.OUT_OF_SPOTLIGHT));
        }
    }

    public void enterStage() {
        if (!isActive()) {
            getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.BEFORE_ENTER_STAGE));
            enterStageWithoutEvents();
            getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.AFTER_ENTER_STAGE));
        }
    }

    public void exitStage() {
        if (isActive()) {
            getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.BEFORE_EXIT_STAGE));
            exitStageWithoutEvents();
            getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.AFTER_EXIT_STAGE));
        }
    }


    protected void exitStageWithoutEvents() {
        memory.clear();
        this.active = false;
    }

    protected void enterStageWithoutEvents() {
        this.active = true;
    }

    public UserTrackingScope getScope() {
        return scope;
    }

    public <T> T recallImmediately(String name) {
        return memory.recall(name);
    }


}
