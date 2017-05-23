//package cucumber.screenplay;
//
//import cucumber.api.java.en.When;
//import cucumber.screenplay.annotations.ProducesEmbedding;
//import cucumber.screenplay.annotations.Step;
//
//import static cucumber.screenplay.ScreenplayPhrases.actorNamed;
//import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom;
//
//public class MockingSteps {
//    private DownstreamSystem downstreamSystem = Mockito.mock(DownstreamSystem.class);
//
//    @When("^two verifications are performed successfully$")
//    public void twoVerificationsArePerformedSuccessfully() throws Throwable {
//        Mockito.when(downstreamSystem.doStuff(10)).thenReturn(" did stuff");
//        downstreamSystem.doStuff(10);
//        Actor johnSmith = actorNamed("John Smith");
//        forRequestsFrom(johnSmith).verifyThat(
//                aVerificationSucceeds());
//    }
//
//    private DownstreamVerification aVerificationSucceeds() {
//        return new DownstreamVerification() {
//            @Override
//            public void performOnStage(ActorOnStage actorOnStage) {
//                Mockito.verify(downstreamSystem).doStuff(10);
//            }
//
//        };
//    }
//
//    public static interface DownstreamSystem {
//        String doStuff(Integer i);
//    }
//
//
//    @When("^two stubs are configured successfully$")
//    public void thenTwoQuestionsAnsweredSuccessully() {
//        Actor johnSmith = actorNamed("John Smith");
//        forRequestsFrom(johnSmith).allow(
//                aStubbedCallToSucceed());
//    }
//
//    private DownstreamStub aStubbedCallToSucceed() {
//        return new DownstreamStub() {
//            @Override
//            @Step("DownstreamSystem.doStuff()")
//            public void performOnStage(ActorOnStage actorOnStage) {
//                Mockito.when(downstreamSystem.doStuff(10)).thenReturn(actorOnStage.getActor().getName() + " did stuff");
//                stubDescription = "Mock downstreamSystem.doStuff(10)).thenReturn(\"" + actorOnStage.getActor().getName() + " did stuff\")";
//            }
//
//            @ProducesEmbedding
//            private String stubDescription;
//        };
//    }
//
//
//}
