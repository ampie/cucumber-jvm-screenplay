package com.sbg.bdd.screenplay.wiremock;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.DownstreamVerification;
import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.wiremock.scoped.recording.WireMockContext;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedRequestPatternBuilder;
import com.sbg.bdd.wiremock.scoped.recording.builders.ResponseStrategy;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

public class ScreenPlayRequestPatternBuilder extends ExtendedRequestPatternBuilder<ScreenPlayRequestPatternBuilder> {



    public ScreenPlayRequestPatternBuilder(RequestMethod requestMethod) {
        super(requestMethod);
    }

    public String getDescription() {
        return "a " + getHttpMethod() + " to \"" + getUrlInfo() +"\"";
    }


    public DownstreamVerification wasMade(final Matcher<Integer> countMatcher) {
        return new DownstreamVerification() {
            String description = getDescription();

            @Override
            @Step("#description")
            public void performOnStage(ActorOnStage actorOnStage) {
                WireMockContext verificationContext = new WireMockScreenplayContext(actorOnStage);
                int count = verificationContext.count(ScreenPlayRequestPatternBuilder.this);
                if (!countMatcher.matches(count)) {
                    Description description = new StringDescription();
                    description.appendText("Expected: ")
                            .appendDescriptionOf(countMatcher)
                            .appendText("\n     but: ");
                    countMatcher.describeMismatch(count, description);
                    throw new AssertionError(description.toString());
                }
            }

            public String getDescription() {
                StringDescription description = new StringDescription();
                countMatcher.describeTo(description);
                return "the number of times a " + getHttpMethod() + " request was made to \"" + getUrlInfo() + "\" is " + description.toString();
            }
        };
    }

    @Override
    public ScreenPlayMappingBuilder will(ResponseStrategy strategy) {
        ScreenPlayMappingBuilder builder = new ScreenPlayMappingBuilder(this);
        builder.will(strategy);
        return builder;
    }

    public ScreenPlayMappingBuilder to(ResponseStrategy responseStrategy) {
        return will(responseStrategy);
    }

}
