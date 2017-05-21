package cucumber.scoping;


import cucumber.scoping.annotations.UserInvolvement;
import cucumber.scoping.events.UserEvent;
import cucumber.scoping.screenplay.ScopedActor;
import cucumber.screenplay.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cucumber.scoping.IdGenerator.fromName;
import static cucumber.screenplay.formatter.FormattingActor.extractInfo;

public abstract class UserInScope implements Memory, ActorOnStage {
    protected final ScopedActor actor;
    private String id;
    private boolean active = false;
    private Map<String, UserInScope> usersInScope = new HashMap<>();
    private UserTrackingScope scope;
    private SimpleMemory memory = new SimpleMemory();
    private List<DownstreamVerification> downstreamExpectations = new ArrayList<>();

    public UserInScope(UserTrackingScope scope, ScopedActor actor) {
        this.scope = scope;
        this.id = fromName(actor.getName());
        this.actor = actor;
    }

    public final String getName(){
        return actor.getName();
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
        getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.BEFORE_ENTER));
        enterWithoutEvents();
        getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.AFTER_ENTER));
    }

    public void exit() {
        getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.BEFORE_EXIT));
        exitWithoutEvents();
        getScope().getGlobalScope().broadcast(new UserEvent(this, UserInvolvement.AFTER_EXIT));
    }

    protected void exitWithoutEvents() {
        this.active = true;
    }

    protected void enterWithoutEvents() {
        this.active = true;
    }

    public void complete() {
        this.active = false;
    }

    public UserTrackingScope getScope() {
        return scope;
    }

    public void evaluateVerificationRules() {

    }

    public <T> T recallImmediately(String name) {
        return memory.recall(name);
    }

    public void allow(DownstreamStub... downstreamStubs) {
        actor.performSteps(extractInfo("Allow", downstreamStubs, this));
    }

    public void expect(DownstreamExpectation... downstreamExpectations) {
        actor.performSteps(extractInfo("Expect", downstreamExpectations, this));
        for (DownstreamExpectation expectation : downstreamExpectations) {
            this.downstreamExpectations.add(expectation.getVerification());
        }
    }

    public void verifyThat(DownstreamVerification... downstreamVerifications) {
        actor.performSteps(extractInfo("Verify", downstreamVerifications, this));
    }

    public void evaluateExpectations() {
        actor.performSteps(extractInfo("Verify", downstreamExpectations.toArray(), this));
    }

    public Actor getActor() {
        return actor;
    }
}
