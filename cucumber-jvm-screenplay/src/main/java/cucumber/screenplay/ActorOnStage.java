package cucumber.screenplay;


public interface ActorOnStage extends Memory {
    void allow(DownstreamStub... downstreamStubs);
    void expect(DownstreamExpectation...downstreamExpectations);
    void verifyThat(DownstreamVerification... downstreamVerifications);
    void evaluateExpectations();
    Actor getActor();
}
