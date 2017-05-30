package com.sbg.bdd.screenplay.wiremock;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.DownstreamVerification;
import com.sbg.bdd.wiremock.scoped.recording.WireMockContext;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedRequestPatternBuilder;
import com.sbg.bdd.wiremock.scoped.recording.builders.ResponseStrategy;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

public class ScreenPlayRequestPatternBuilder extends ExtendedRequestPatternBuilder<ScreenPlayRequestPatternBuilder> {


    public ScreenPlayRequestPatternBuilder(ExtendedRequestPatternBuilder builder) {
        super(builder);
    }

    public ScreenPlayRequestPatternBuilder(RequestMethod requestMethod) {
        super(requestMethod);
    }

    public ScreenPlayRequestPatternBuilder(ExtendedRequestPatternBuilder requestPatternBuilder, RequestMethod method) {
        super(requestPatternBuilder, method);
    }

    public DownstreamVerification wasMade(final Matcher<Integer> countMatcher) {
        return new DownstreamVerification() {
            @Override
            public void performOnStage(ActorOnStage actorOnStage) {
                WireMockContext verificationContext = new WireMockScopeContext(actorOnStage);
                int count = verificationContext.count(ScreenPlayRequestPatternBuilder.this);
                if (!countMatcher.matches(count)) {
                    StringDescription description = new StringDescription();
                    countMatcher.describeMismatch(count, description);
                    throw new AssertionError(description.toString());
                }
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
