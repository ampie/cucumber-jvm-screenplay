package cucumber.screenplay.internal;

import cucumber.screenplay.*;

import java.util.ArrayList;
import java.util.List;

public class BaseActorOnStage implements ActorOnStage {
    private Actor actor;
    private Memory memory = new SimpleMemory();
    private List<DownstreamVerification> downstreamExpectations =new ArrayList<>();

    public BaseActorOnStage(Actor actor) {
        this.actor = actor;
        actor.setCurrentRole(this);
    }

    @Override
    public void allow(DownstreamStub... downstreamStubs) {
        actor.performSteps("Allow", this,downstreamStubs);
    }


    @Override
    public void expect(DownstreamExpectation... downstreamExpectations) {
        DownstreamStub[] stubs = new DownstreamStub[downstreamExpectations.length];
        for (int i = 0; i < downstreamExpectations.length; i++) {
             stubs[i]=downstreamExpectations[i].getStub();
            this.downstreamExpectations.add(downstreamExpectations[i].getVerification());
        }
        actor.performSteps("Expect", this, stubs);
    }

    @Override
    public void verifyThat(DownstreamVerification... downstreamVerifications) {
        actor.performSteps("Verify", this,downstreamVerifications);
    }

    @Override
    public void evaluateExpectations() {
        actor.performSteps("Verify", this,downstreamExpectations.toArray());
    }

    @Override
    public Actor getActor() {
        return actor;
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
        return memory.recall(name);
    }

    @Override
    public <T> T recall(Class<T> clzz) {
        return memory.recall(clzz);
    }
}
