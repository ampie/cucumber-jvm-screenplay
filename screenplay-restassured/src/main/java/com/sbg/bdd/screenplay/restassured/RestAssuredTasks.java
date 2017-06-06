package com.sbg.bdd.screenplay.restassured;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Question;
import com.sbg.bdd.screenplay.core.Task;
import com.sbg.bdd.screenplay.core.annotations.Step;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;

import java.net.MalformedURLException;
import java.net.URL;

import static com.sbg.bdd.screenplay.core.actors.OnStage.*;

public class RestAssuredTasks {
    public static Task get(final String uri, final RequestSpecification spec) {
        return new Task() {
            @Override
            public <T extends Actor> T performAs(T actor) {
                ActorOnStage actorOnStage = shineSpotlightOn(actor);
                Response response = spec.config(RestAssuredConfig.config())
                        .filter(new CorrelationFilter()).get(uri);
                actorOnStage.remember("lastResponse", response);
                return actor;
            }
        };
    }
    public static Task put(final String urii, final RequestSpecification spec) {
        return new Task() {
            String uri=urii;
            String body=((FilterableRequestSpecification)spec).getBody().toString();
            @Override
            @Step("Send a PUT request to #uri with body '#body'")
            public <T extends Actor> T performAs(T actor) {
                ActorOnStage actorOnStage = shineSpotlightOn(actor);
                Response response = spec.config(RestAssuredConfig.config())
                        .filter(new CorrelationFilter())
                        .put(uri);
                actorOnStage.remember("lastResponse", response);
                return actor;
            }
        };
    }
    public static Task post(final String uri, final RequestSpecification spec) {
        return new Task() {
            @Override
            public <T extends Actor> T performAs(T actor) {
                ActorOnStage actorOnStage = shineSpotlightOn(actor);
                Response response = spec.config(RestAssuredConfig.config())
                        .filter(new CorrelationFilter()).post(uri);
                actorOnStage.remember("lastResponse", response);
                return actor;
            }
        };
    }


    public static Task delete(final String uri, final RequestSpecification spec) {
        return new Task() {
            @Override
            public <T extends Actor> T performAs(T actor) {
                ActorOnStage actorOnStage = shineSpotlightOn(actor);
                Response response = spec.config(RestAssuredConfig.config())
                        .filter(new CorrelationFilter()).delete(uri);
                actorOnStage.remember("lastResponse", response);
                return actor;
            }
        };
    }

    public static void thenFor(Actor actor, ResponseConsequence... consequences) {
        actor.should(consequences);
    }
    public static <T> Question<T> bodyAs(final Class<T> clss){
        return new Question<T>() {
            @Override
            public T answeredBy(Actor actor) {
                ActorOnStage actorOnStage = callActorToStage(actor);
                Response response = actorOnStage.recall("lastResponse");
                ValidatableResponse then = response.then();
                return then.extract().body().as(clss);
            }
        };


    }

    public static ValidatableResponse theLastResponse() {
        ActorOnStage actorOnStage = theActorInTheSpotlight();
        Response response = actorOnStage.recall("lastResponse");
        ValidatableResponse then = response.then();
        return then;
    }

    public static ResponseConsequence expect() {
        return new ResponseConsequence(RestAssured.expect());
    }
    public static ResponseConsequence assertThat() {
        return new ResponseConsequence(RestAssured.expect());
    }

}
