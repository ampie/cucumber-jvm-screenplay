package com.sbg.bdd.cucumber.screenplay.core;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.DownstreamStub;
import com.sbg.bdd.screenplay.core.DownstreamVerification;
import com.sbg.bdd.screenplay.core.annotations.ProducesAttachment;
import com.sbg.bdd.screenplay.core.annotations.Step;
import cucumber.api.java.en.When;
import org.mockito.Mockito;

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed;
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom;

public class MockingSteps {
    private DownstreamSystem downstreamSystem = Mockito.mock(DownstreamSystem.class);

    @When("^two verifications are performed successfully$")
    public void twoVerificationsArePerformedSuccessfully() throws Throwable {
        Mockito.when(downstreamSystem.doStuff(10)).thenReturn(" did stuff");
        downstreamSystem.doStuff(10);
        Actor johnSmith = actorNamed("John Smith");
        forRequestsFrom(johnSmith).verifyThat(
                aVerificationSucceeds());
    }

    private DownstreamVerification aVerificationSucceeds() {
        return new DownstreamVerification() {
            @Override
            public void performOnStage(ActorOnStage actorOnStage) {
                Mockito.verify(downstreamSystem).doStuff(10);
            }

        };
    }

    public static interface DownstreamSystem {
        String doStuff(Integer i);
    }


    @When("^two stubs are configured successfully$")
    public void thenTwoQuestionsAnsweredSuccessully() {
        Actor johnSmith = actorNamed("John Smith");
        forRequestsFrom(johnSmith).allow(
                aStubbedCallToSucceed());
    }

    private DownstreamStub aStubbedCallToSucceed() {
        return new DownstreamStub() {
            @Override
            @Step("DownstreamSystem.doStuff()")
            public void performOnStage(ActorOnStage actorOnStage) {
                Mockito.when(downstreamSystem.doStuff(10)).thenReturn(actorOnStage.getActor().getName() + " did stuff");
                stubDescription = "Mock downstreamSystem.doStuff(10)).thenReturn(\"" + actorOnStage.getActor().getName() + " did stuff\")";
            }

            @ProducesAttachment
            private String stubDescription;
        };
    }


}
