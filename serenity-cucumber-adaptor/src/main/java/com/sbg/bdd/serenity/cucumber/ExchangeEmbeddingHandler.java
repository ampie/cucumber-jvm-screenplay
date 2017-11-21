package com.sbg.bdd.serenity.cucumber;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedExchange;
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedRequest;
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedResponse;
import net.serenitybdd.core.rest.RestMethod;
import net.serenitybdd.core.rest.RestQuery;
import net.serenitybdd.cucumber.adaptor.EmbeddingHandler;
import net.thucydides.core.steps.ExecutedStepDescription;
import net.thucydides.core.steps.StepEventBus;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExchangeEmbeddingHandler implements EmbeddingHandler {
    private static final Logger LOGGER = Logger.getLogger(ExchangeEmbeddingHandler.class.getName());

    @Override
    public boolean attemptHandling(String mimeType, byte[] data) {
        if (mimeType.endsWith("json")) {
            String json = new String(data);
            if (json.contains("\"request\" : {") && json.contains("\"response\" : {") && json.contains("\"nestedExchanges\" : [")) {
                CollectionLikeType type = Json.getObjectMapper().getTypeFactory().constructCollectionType(List.class, RecordedExchange.class);
                ObjectMapper mapper = Json.getObjectMapper();
                try {
                    List<RecordedExchange> exchanges = mapper.readValue(json, type);
                    if (exchanges.size() == 1) {
                        StepEventBus.getEventBus().getBaseStepListener().addRestQuery(toRestQuery(exchanges.get(0)));
                        logExchanges(exchanges.get(0).getNestedExchanges());
                    } else {
                        logExchanges(exchanges);
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

    private void logExchanges(List<RecordedExchange> exchanges) {
        for (RecordedExchange exchange : exchanges) {
            StepEventBus.getEventBus().stepStarted(ExecutedStepDescription.withTitle(exchange.getRequest().getAbsoluteUrl()));
            StepEventBus.getEventBus().getBaseStepListener().addRestQuery(toRestQuery(exchange));
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
        String bodyAsString = response.getBodyAsString();

        HttpHeaders headers = new HttpHeaders(response.getHeaders()).plus(new HttpHeader("Response-Code", "" + describeStatus(response)));
        RestQuery result = RestQuery.withMethod(RestMethod.valueOf(request.getMethod().getName()))
                .andPath(request.getUrl())
                .withContent(formatBody(request.getHeaders().getContentTypeHeader(), request.getBodyAsString()))
                .withRequestHeaders(Json.write(request.getHeaders()))
                .withParameters(parameters)
                .withStatusCode(response.getStatus())
                .withResponseHeaders(Json.write(headers))
                .withResponse(formatBody(headers.getContentTypeHeader(), bodyAsString));
        if (request.getAllHeaderKeys().contains(ContentTypeHeader.KEY)) {
            result = result.withContentType(request.getHeader(ContentTypeHeader.KEY));
        }
        return result;
    }

    private String describeStatus(RecordedResponse response) {
        Field[] declaredFields = HttpURLConnection.class.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if(!declaredField.isAnnotationPresent(Deprecated.class) && declaredField.getType() == int.class && Modifier.isStatic(declaredField.getModifiers()) && Modifier.isPublic(declaredField.getModifiers())){
                try {
                    Integer constant= (Integer) declaredField.get(null);
                    if(constant.intValue() == response.getStatus()){
                        return declaredField.getName();
                    }
                } catch (Exception e) {
                }
            }
        }
        return "" + response.getStatus();
    }

    private String formatBody(ContentTypeHeader contentTypeHeader, String bodyAsString) {
        if (contentTypeHeader == null || !contentTypeHeader.isPresent()) {
            return bodyAsString;
        } else if (contentTypeHeader.mimeTypePart().contains("xml")) {
            return formatXml(bodyAsString);
        } else if (contentTypeHeader.mimeTypePart().contains("json")) {
            return formatJson(bodyAsString);
        } else {
            return bodyAsString;
        }
    }

    private String formatJson(String bodyAsString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(bodyAsString));
        } catch (IOException e) {
            //we tried our best
            return bodyAsString;
        }
    }

    private String formatXml(String bodyAsString) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StreamResult result = new StreamResult(new StringWriter());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(bodyAsString));
            DOMSource source = new DOMSource(db.parse(is));
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (Exception e) {
            //we tried our best
            return bodyAsString;
        }
    }
}
