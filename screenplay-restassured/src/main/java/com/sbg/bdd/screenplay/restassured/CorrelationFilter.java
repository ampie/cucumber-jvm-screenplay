package com.sbg.bdd.screenplay.restassured;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.wiremock.CorrelationPath;
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory;
import com.sbg.bdd.wiremock.scoped.integration.HeaderName;
import com.sbg.bdd.wiremock.scoped.integration.WireMockCorrelationState;
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedMappingBuilder;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedRequestPatternBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.net.URI;
import java.util.Map;

import static com.sbg.bdd.screenplay.core.actors.OnStage.theActorInTheSpotlight;

public class CorrelationFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpecification, FilterableResponseSpecification responseSpec, FilterContext filterContext) {
        RecordingWireMockClient wm = OnStage.performance().recall("recordingWireMockClient");
        StubMapping mapping = buildProxyMapping(URI.create(requestSpecification.getURI()));
        wm.register(mapping);
        propagateCorrelationState(requestSpecification);
        String redirectedPath = wm.baseUrl() + URI.create(requestSpecification.getURI()).getPath();
        Response response = filterContext.next(requestSpecification.path(redirectedPath), responseSpec);
        wm.removeStubMapping(mapping);
        syncLocalCorrelationState(response);
        return response;

    }

    private StubMapping buildProxyMapping(URI uri) {
        ExtendedMappingBuilder builder = new ExtendedMappingBuilder(new ExtendedRequestPatternBuilder(RequestMethod.ANY).to(uri.getPath()));
        builder.willReturn(WireMock.aResponse().proxiedFrom(uri.getScheme() + "://" + uri.getAuthority())).atPriority(1);
        builder.getRequestPatternBuilder().prepareForBuild(null);
        builder.getRequestPatternBuilder().ensureScopePath(WireMock.equalTo(CorrelationPath.of(theActorInTheSpotlight())));
        return builder.build();
    }

    private void propagateCorrelationState(FilterableRequestSpecification requestSpecification) {
        requestSpecification.header(HeaderName.ofTheCorrelationKey(), CorrelationPath.of(theActorInTheSpotlight()));
        WireMockCorrelationState currentCorrelationState = DependencyInjectionAdaptorFactory.getAdaptor().getCurrentCorrelationState();
        if (currentCorrelationState.isSet()) {
            String key = URI.create(requestSpecification.getURI()) + requestSpecification.getMethod();
            requestSpecification
                    .header(HeaderName.ofTheCorrelationKey(), currentCorrelationState.getCorrelationPath())
                    .header(HeaderName.ofTheSequenceNumber(), currentCorrelationState.getNextSequenceNumberFor(key).toString());
            if (currentCorrelationState.shouldProxyUnmappedEndpoints()) {
                requestSpecification.header(HeaderName.toProxyUnmappedEndpoints(), "true");
            }
            for (Map.Entry<String, Integer> entry : currentCorrelationState.getSequenceNumbers().entrySet()) {
                requestSpecification.header(HeaderName.ofTheServiceInvocationCount(), entry.getKey() + "|" + entry.getValue());
            }
        }

    }

    private void syncLocalCorrelationState(Response response) {
        WireMockCorrelationState currentCorrelationState = DependencyInjectionAdaptorFactory.getAdaptor().getCurrentCorrelationState();
        if (currentCorrelationState.isSet()) {
            Headers headers = response.getHeaders();
            if (headers != null && headers.hasHeaderWithName(HeaderName.ofTheServiceInvocationCount())) {
                Iterable<String> o = headers.getValues(HeaderName.ofTheServiceInvocationCount());
                for (String s : o) {
                    String[] split = s.split("\\|");
                    currentCorrelationState.initSequenceNumberFor(split[0], Integer.valueOf(split[1]));
                }
            }
        }
    }
}
