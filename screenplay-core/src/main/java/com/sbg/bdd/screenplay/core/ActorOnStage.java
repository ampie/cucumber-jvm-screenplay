package com.sbg.bdd.screenplay.core;


public interface ActorOnStage extends Memory {
    void allow(DownstreamStub... downstreamStubs);

    void expect(DownstreamExpectation... downstreamExpectations);

    void verifyThat(DownstreamVerification... downstreamVerifications);

    void evaluateExpectations();

    Actor getActor();

    Scene getScene();

    <T> T recallImmediately(String variableName);
    
    String getId();
}
