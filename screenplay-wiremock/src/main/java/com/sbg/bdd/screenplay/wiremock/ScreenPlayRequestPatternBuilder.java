package com.sbg.bdd.screenplay.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.DownstreamVerification;
import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.wiremock.scoped.client.WireMockContext;
import com.sbg.bdd.wiremock.scoped.client.builders.ExtendedMappingBuilder;
import com.sbg.bdd.wiremock.scoped.client.builders.ExtendedRequestPatternBuilder;
import com.sbg.bdd.wiremock.scoped.client.builders.ResponseStrategy;
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
    public ScreenPlayRequestPatternBuilder withQueryParam(String key, StringValuePattern valuePattern) {
        return (ScreenPlayRequestPatternBuilder) super.withQueryParam(key, valuePattern);
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
    public ScreenPlayMappingBuilder toReturn(ResponseDefinitionBuilder response) {
        ScreenPlayMappingBuilder ruleBuilder = new ScreenPlayMappingBuilder(this);
        ruleBuilder.willReturn(response);
        return ruleBuilder;
    }
    public ScreenPlayMappingBuilder willReturn(ResponseDefinitionBuilder response) {
        return toReturn(response);
    }

}
