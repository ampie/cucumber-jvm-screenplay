package cucumber.screenplay.formatter;

import cucumber.screenplay.*;
import cucumber.screenplay.Actor;

import java.util.ArrayList;
import java.util.List;

import static cucumber.screenplay.formatter.FormattingActor.extractInfo;

public class FormattingActorOnStage implements ActorOnStage {
    private FormattingActor actor;
    private Memory memory = new SimpleMemory();
    private List<DownstreamVerification> downstreamExpectations =new ArrayList<>();

    public FormattingActorOnStage(FormattingActor actor) {
        this.actor = actor;
        actor.setCurrentRole(this);
    }

    @Override
    public void allow(DownstreamStub... downstreamStubs) {
        actor.performSteps(extractInfo("Allow", downstreamStubs,this));
    }


    @Override
    public void expect(DownstreamExpectation... downstreamExpectations) {
        actor.performSteps(extractInfo("Expect", downstreamExpectations,this));
        for (DownstreamExpectation expectation : downstreamExpectations) {
            this.downstreamExpectations.add(expectation.getVerification());
        }
    }

    @Override
    public void verifyThat(DownstreamVerification... downstreamVerifications) {
        actor.performSteps(extractInfo("Verify", downstreamVerifications,this));
    }

    @Override
    public void evaluateExpectations() {
        actor.performSteps(extractInfo("Verify", downstreamExpectations.toArray(),this));
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
