package com.sbg.bdd.screenplay.restassured;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.screenplay.core.persona.Persona;
import com.sbg.bdd.screenplay.wiremock.WireMockMemories;
import com.sbg.bdd.wiremock.scoped.integration.HeaderName;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;

import static com.sbg.bdd.screenplay.core.actors.OnStage.shineSpotlightOn;

public class PutTask implements RestAssuredTasks.HttpTask {
    private final RequestSpecification spec;
    private String uri;
    private String body;

    public PutTask(String uri, RequestSpecification spec) {
        this(spec);
        this.uri = uri;
    }

    public PutTask(RequestSpecification spec) {

        this.spec = spec;
        body = ((FilterableRequestSpecification) spec).getBody().toString();
    }

    @Override
    public RestAssuredTasks.HttpTask to(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    @Step("send a PUT request to #uri with body '#body'")
    public <T extends Actor> T performAs(T actor) {
        ActorOnStage actorOnStage = shineSpotlightOn(actor);
        prependBaseUrl(actorOnStage, uri, spec);
        Response response = spec
                .filter(new CorrelationFilter())
                .put(uri);
        actorOnStage.remember(RestAssuredTasks.LAST_RESPONSE, response);
        return actor;
    }

    public static void prependBaseUrl(ActorOnStage actorOnStage, String uri, RequestSpecification spec) {
        if(actorOnStage.getActor().recall("persona")!=null){
            Persona<?> p =  actorOnStage.getActor().getPersona();
            spec.header(new Header(HeaderName.ofTheSessionToken(),p.getSessionToken()));
        }
        if(contentTypeNotSpecified((FilterableRequestSpecification) spec)){
            spec.contentType(ContentType.JSON);
        }
        if (baseUrlNotSpecified((FilterableRequestSpecification) spec) && !uri.startsWith("http")) {
            String baseUri = WireMockMemories.recallFrom(actorOnStage).theBaseUrlOfTheServiceUnderTest();
            if (baseUri != null) {
                spec.baseUri(baseUri);
            }
        }
    }

    private static boolean contentTypeNotSpecified(FilterableRequestSpecification spec) {
        return spec.getContentType() == null || spec.getContentType().equals(ContentType.TEXT.getContentTypeStrings()[0]);
    }

    private static boolean baseUrlNotSpecified(FilterableRequestSpecification spec) {
        return spec.getBaseUri() == null || spec.getBaseUri().equals("http://localhost");
    }
}
