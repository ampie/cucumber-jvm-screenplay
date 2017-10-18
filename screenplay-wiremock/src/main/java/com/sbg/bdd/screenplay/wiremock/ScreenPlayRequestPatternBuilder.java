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
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

public class ScreenPlayRequestPatternBuilder extends ExtendedRequestPatternBuilder<ScreenPlayRequestPatternBuilder> {


    public ScreenPlayRequestPatternBuilder(RequestMethod requestMethod) {
        super(requestMethod);
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        if (getHttpMethod().equals("ANY")) {
            sb.append("any request to ");

        } else {
            sb.append("a " + getHttpMethod() + " to ");
        }
        if (isToAllKnownExternalServices()) {
            if (StringUtils.isEmpty(getEndpointCategory())) {
                sb.append("any known external service");
            } else {
                sb.append("any " + getEndpointCategory() + " service");
            }
        } else {
            if (StringUtils.isEmpty(getPathSuffix())) {
                sb.append("\"" + getUrlInfo() + "\"");
            } else {
                sb.append("\"" + getUrlInfo() + getPathSuffix() + "\"");
            }
        }
        return sb.toString();
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
