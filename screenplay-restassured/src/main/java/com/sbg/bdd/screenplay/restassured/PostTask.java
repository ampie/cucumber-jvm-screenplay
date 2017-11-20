package com.sbg.bdd.screenplay.restassured;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.annotations.Step;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;

import static com.sbg.bdd.screenplay.core.actors.OnStage.shineSpotlightOn;
import static com.sbg.bdd.screenplay.restassured.PutTask.prependBaseUrl;

public class PostTask implements RestAssuredTasks.HttpTask {
    private RequestSpecification spec;
    private String uri;

    public PostTask(String uri, RequestSpecification spec) {
        this(spec);
        this.uri = uri;
    }

    public PostTask(RequestSpecification spec) {
        this.spec = spec;
        body = ((FilterableRequestSpecification) spec).getBody().toString();
    }

    @Override
    public RestAssuredTasks.HttpTask to(String uri) {
        this.uri = uri;
        return this;
    }

    String body;

    @Override
    @Step("send a POST request to #uri")
    public <T extends Actor> T performAs(T actor) {
        ActorOnStage actorOnStage = shineSpotlightOn(actor);
        prependBaseUrl(actorOnStage, uri, spec);
        Response response = spec.filter(new CorrelationFilter()).post(uri);
        actorOnStage.remember(RestAssuredTasks.LAST_RESPONSE, response);
        return actor;
    }
}
