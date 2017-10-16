package com.sbg.bdd.serenity.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import net.serenitybdd.core.rest.RestMethod;
import net.serenitybdd.core.rest.RestQuery;
import net.serenitybdd.cucumber.adaptor.EmbeddingHandler;
import net.thucydides.core.steps.ExecutedStepDescription;
import net.thucydides.core.steps.StepEventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StubMappingEmbeddingHandler implements EmbeddingHandler {
    private static final Logger LOGGER = Logger.getLogger(StubMappingEmbeddingHandler.class.getName());

    @Override
    public boolean attemptHandling(String mimeType, byte[] data) {
        if (mimeType.endsWith("json")) {
            String json = new String(data);
            if (json.contains("\"request\" : {") && json.contains("\"response\" : {") && json.contains("\"id\" : ")) {
                CollectionLikeType type = Json.getObjectMapper().getTypeFactory().constructCollectionType(List.class, StubMapping.class);
                ObjectMapper mapper = Json.getObjectMapper();
                try {
                    List<StubMapping> stubMappings = mapper.readValue(json, type);
                    if (stubMappings.size() == 1) {
                        StepEventBus.getEventBus().getBaseStepListener().addRestQuery(toRestQuery(stubMappings.get(0)));
                    } else {
                        logstubMappings(stubMappings);
                    }
                    return true;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Could not read embedding", e);
                    return false;
                }
            }
        }
        return false;
    }

    private void logstubMappings(List<StubMapping> stubMappings) {
        for (StubMapping stubMapping : stubMappings) {
            StepEventBus.getEventBus().stepStarted(ExecutedStepDescription.withTitle(stubMapping.getRequest().getUrlMatcher().getExpected()));
            StepEventBus.getEventBus().getBaseStepListener().addRestQuery(toRestQuery(stubMapping));
            StepEventBus.getEventBus().stepFinished();
        }
    }

    private RestQuery toRestQuery(StubMapping stubMapping) {
        RequestPattern request = stubMapping.getRequest();
        ResponseDefinition response = stubMapping.getResponse();
        Map<String, String> parameters = new HashMap<>();
        if (request.getQueryParameters() != null) {
            for (Map.Entry<String, MultiValuePattern> entry : request.getQueryParameters().entrySet()) {
                parameters.put(entry.getKey(), entry.getValue().getValuePattern().getExpected());
            }
        }
        RestMethod method = RestMethod.OPTIONS;
        try {
            method = RestMethod.valueOf(request.getMethod().getName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,e.toString());
        }
        RestQuery result = RestQuery.withMethod(method)
                .andPath(request.getUrl())
                .withContent(Json.write(request.getBodyPatterns()))
                .withRequestHeaders(Json.write(request.getHeaders()))
                .withParameters(parameters)
                .withStatusCode(response.getStatus())
                .withResponseHeaders(Json.write(response.getHeaders()))
                .withResponse(response.getBody());
        if (request.getHeaders()!=null && request.getHeaders().containsKey(ContentTypeHeader.KEY)) {
            result = result.withContentType(request.getHeaders().get(ContentTypeHeader.KEY).getExpected());
        }
        return result;
    }
}
