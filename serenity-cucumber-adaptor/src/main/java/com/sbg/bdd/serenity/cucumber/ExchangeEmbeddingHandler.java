package com.sbg.bdd.serenity.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedExchange;
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedRequest;
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedResponse;
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

public class ExchangeEmbeddingHandler implements EmbeddingHandler {
    Logger LOGGer = Logger.getLogger(ExchangeEmbeddingHandler.class.getName());

    @Override
    public boolean attemptHandling(String mimeType, byte[] data) {
        if (mimeType.endsWith("json")) {
            String json = new String(data);
            if (json.contains("\"request\":") && json.contains("\"response\":")) {
                CollectionLikeType type = Json.getObjectMapper().getTypeFactory().constructCollectionType(List.class, RecordedExchange.class);
                ObjectMapper mapper = Json.getObjectMapper();
                try {
                    List<RecordedExchange> exchanges = mapper.readValue(json, type);
                    logExchanges(exchanges);
                    return true;
                } catch (Exception e) {
                    LOGGer.log(Level.WARNING, "Could not read embedding", e);
                    return false;
                }
            }
        }
        return false;
    }

    private void logExchanges(List<RecordedExchange> exchanges) {
        for (RecordedExchange exchange : exchanges) {
            StepEventBus.getEventBus().stepStarted(ExecutedStepDescription.withTitle(exchange.getRequest().getAbsoluteUrl()));
            StepEventBus.getEventBus().getBaseStepListener().recordRestQuery(toRestQuery(exchange));
            logExchanges(exchange.getNestedExchanges());
            StepEventBus.getEventBus().getBaseStepListener().recordStepDuration(exchange.getDuration());
            StepEventBus.getEventBus().stepFinished();
        }
    }

    private RestQuery toRestQuery(RecordedExchange exchange) {
        RecordedRequest request = exchange.getRequest();
        RecordedResponse response = exchange.getResponse();
        Map<String, String> parameters = new HashMap<>();
        for (Map.Entry<String, QueryParameter> entry : request.getQueryParameters().entrySet()) {
            parameters.put(entry.getKey(), entry.getValue().firstValue());
        }
        return RestQuery.withMethod(RestMethod.valueOf(request.getMethod().getName()))
                .andPath(request.getUrl())
                .withContent(request.getBodyAsString())
                .withContentType(request.getHeader(ContentTypeHeader.KEY))
                .withRequestHeaders(Json.write(request.getHeaders()))
                .withParameters(parameters)
                .withStatusCode(response.getStatus())
                .withResponseHeaders(Json.write(response.getHeaders()))
                .withResponse(response.getBodyAsString());
    }
}
