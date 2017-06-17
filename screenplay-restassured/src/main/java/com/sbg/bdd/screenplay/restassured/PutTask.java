package com.sbg.bdd.screenplay.restassured;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.annotations.Step;
import io.restassured.config.RestAssuredConfig;
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
        this.uri=uri;
        return this;
    }

    @Override
    @Step("send a PUT request to #uri with body '#body'")
    public <T extends Actor> T performAs(T actor) {
        ActorOnStage actorOnStage = shineSpotlightOn(actor);
        Response response = spec.config(RestAssuredConfig.config())
                .filter(new CorrelationFilter())
                .put(uri);
        actorOnStage.remember(RestAssuredTasks.LAST_RESPONSE, response);
        return actor;
    }
}
