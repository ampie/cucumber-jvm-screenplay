package com.sbg.bdd.screenplay.restassured;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.annotations.Step;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static com.sbg.bdd.screenplay.core.actors.OnStage.shineSpotlightOn;

public class DeleteTask implements RestAssuredTasks.HttpTask {
    final RequestSpecification spec;
    private String uri;

    public DeleteTask(String uri, RequestSpecification spec) {
        this.spec = spec;
        this.uri = uri;
    }

    @Override
    public RestAssuredTasks.HttpTask to(String uri) {
        this.uri=uri;
        return this;
    }


    @Override
    @Step("send a DELETE request to #uri")
    public <T extends Actor> T performAs(T actor) {
        ActorOnStage actorOnStage = shineSpotlightOn(actor);
        Response response = spec.config(RestAssuredConfig.config())
                .filter(new CorrelationFilter()).delete(uri);
        actorOnStage.remember(RestAssuredTasks.LAST_RESPONSE, response);
        return actor;
    }
}
