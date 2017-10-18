package com.sbg.bdd.screenplay.restassured;


import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.wiremock.CorrelationPath;
import com.sbg.bdd.screenplay.wiremock.WireMockScreenplayContext;
import com.sbg.bdd.wiremock.scoped.admin.model.ExtendedStubMapping;
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory;
import com.sbg.bdd.wiremock.scoped.integration.HeaderName;
import com.sbg.bdd.wiremock.scoped.integration.URLHelper;
import com.sbg.bdd.wiremock.scoped.integration.WireMockCorrelationState;
import com.sbg.bdd.wiremock.scoped.client.ScopedWireMockClient;
import com.sbg.bdd.wiremock.scoped.client.builders.ExtendedMappingBuilder;
import com.sbg.bdd.wiremock.scoped.client.builders.ExtendedRequestPatternBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import static com.sbg.bdd.screenplay.core.actors.OnStage.theActorInTheSpotlight;

public class CorrelationFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpecification, FilterableResponseSpecification responseSpec, FilterContext filterContext) {
        WireMockCorrelationState currentCorrelationState = DependencyInjectionAdaptorFactory.getAdaptor().getCurrentCorrelationState();
        ScopedWireMockClient wm = OnStage.performance().recall(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT);
        StubMapping mapping = buildProxyMapping(URI.create(requestSpecification.getURI()));
        wm.register(mapping);
        propagateCorrelationState(requestSpecification, currentCorrelationState);
        String redirectedBaseUri = redirectedBaseUri(requestSpecification, currentCorrelationState);
        Response response = filterContext.next((FilterableRequestSpecification) requestSpecification.baseUri(redirectedBaseUri), responseSpec);
        wm.removeStubMapping(mapping);
        syncLocalCorrelationState(response);
        return response;

    }

    private String redirectedBaseUri(FilterableRequestSpecification requestSpecification, WireMockCorrelationState currentCorrelationState) {
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

    private void propagateCorrelationState(FilterableRequestSpecification requestSpecification, WireMockCorrelationState currentCorrelationState) {
        requestSpecification.header(HeaderName.ofTheCorrelationKey(), CorrelationPath.of(theActorInTheSpotlight()));
        if (currentCorrelationState.isSet()) {
            try {
                URL originalUrl = new URL(requestSpecification.getURI());
                String key = URLHelper.identifier(originalUrl, requestSpecification.getMethod());
                String sequenceNumber = currentCorrelationState.getNextSequenceNumberFor(key).toString();


                requestSpecification
                        .header(HeaderName.ofTheOriginalUrl(), new URL(URLHelper.identifier(originalUrl)).toExternalForm())
                        .header(HeaderName.ofTheSequenceNumber(), sequenceNumber);
                if (currentCorrelationState.shouldProxyUnmappedEndpoints()) {
                    requestSpecification.header(HeaderName.toProxyUnmappedEndpoints(), "true");
                }
                if (requestSpecification.getHeaders().getValues(HeaderName.ofTheCorrelationKey())==null || requestSpecification.getHeaders().getValues(HeaderName.ofTheCorrelationKey()).isEmpty()) {
                    requestSpecification.header(HeaderName.ofTheCorrelationKey(), CorrelationPath.of(theActorInTheSpotlight()));
                }
                for (Map.Entry<String, Integer> entry : currentCorrelationState.getSequenceNumbers().entrySet()) {
                    requestSpecification.header(HeaderName.ofTheServiceInvocationCount(), entry.getKey() + "|" + entry.getValue());
                }
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
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
