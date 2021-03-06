package com.sbg.bdd.screenplay.restassured;


import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.wiremock.CorrelationPath;
import com.sbg.bdd.screenplay.wiremock.WireMockScreenplayContext;
import com.sbg.bdd.wiremock.scoped.integration.*;
import com.sbg.bdd.wiremock.scoped.client.ScopedWireMockClient;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static com.sbg.bdd.screenplay.core.actors.OnStage.theActorInTheSpotlight;

public class CorrelationFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpecification, FilterableResponseSpecification responseSpec, FilterContext filterContext) {
        RuntimeCorrelationState currentCorrelationState = DependencyInjectionAdaptorFactory.getAdaptor().getCurrentCorrelationState();
        ScopedWireMockClient wm = OnStage.performance().recall(WireMockScreenplayContext.SCOPED_WIRE_MOCK_CLIENT);
        StubMapping mapping = buildProxyMapping(URI.create(requestSpecification.getURI()));
        wm.register(mapping);
        propagateCorrelationState(requestSpecification, currentCorrelationState);
        String redirectedBaseUri = redirectedBaseUri(requestSpecification, currentCorrelationState);
        Response response = filterContext.next((FilterableRequestSpecification) requestSpecification.baseUri(redirectedBaseUri), responseSpec);
        wm.removeStubMapping(mapping);
        return response;

    }

    private String redirectedBaseUri(FilterableRequestSpecification requestSpecification, RuntimeCorrelationState currentCorrelationState) {
        try {
            return URLHelper.replaceBaseUrl(URI.create(requestSpecification.getBaseUri()).toURL(), currentCorrelationState.getWireMockBaseUrl()).toExternalForm();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private StubMapping buildProxyMapping(URI uri) {
        MappingBuilder builder = WireMock.any(WireMock.urlPathEqualTo(uri.getPath()));
        builder.willReturn(WireMock.aResponse().proxiedFrom(uri.getScheme() + "://" + uri.getAuthority()));
        builder.atPriority(1);
        builder.withHeader(HeaderName.ofTheCorrelationKey(), WireMock.equalTo(CorrelationPath.of(theActorInTheSpotlight())));
        return builder.build();
    }

    private void propagateCorrelationState(FilterableRequestSpecification requestSpecification, RuntimeCorrelationState currentCorrelationState) {
        requestSpecification.header(HeaderName.ofTheCorrelationKey(), CorrelationPath.of(theActorInTheSpotlight()));
        if (currentCorrelationState.isSet()) {
            try {
                URL originalUrl = new URL(requestSpecification.getURI());
                String key = URLHelper.identifier(originalUrl, requestSpecification.getMethod());
                requestSpecification.header(HeaderName.ofTheOriginalUrl(), new URL(URLHelper.identifier(originalUrl)).toExternalForm());
                if (currentCorrelationState.shouldProxyUnmappedEndpoints()) {
                    requestSpecification.header(HeaderName.toProxyUnmappedEndpoints(), "true");
                }
                if (requestSpecification.getHeaders().getValues(HeaderName.ofTheCorrelationKey()) == null || requestSpecification.getHeaders().getValues(HeaderName.ofTheCorrelationKey()).isEmpty()) {
                    requestSpecification.header(HeaderName.ofTheCorrelationKey(), CorrelationPath.of(theActorInTheSpotlight()));
                }
                requestSpecification.header(HeaderName.ofTheThreadContextId(), currentCorrelationState.getCurrentThreadContextId());
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }

    }

}
