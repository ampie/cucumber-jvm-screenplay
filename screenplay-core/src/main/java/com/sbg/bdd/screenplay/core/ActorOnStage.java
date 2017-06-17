package com.sbg.bdd.screenplay.core;


public interface ActorOnStage extends LayeredMemory {
    void allow(DownstreamStub... downstreamStubs);

    void expect(DownstreamExpectation... downstreamExpectations);

    void verifyThat(DownstreamVerification... downstreamVerifications);

    void evaluateExpectations();

    Actor getActor();

    Scene getScene();

    String getId();
}
