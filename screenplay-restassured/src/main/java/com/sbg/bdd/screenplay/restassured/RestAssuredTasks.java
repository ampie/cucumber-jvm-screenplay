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

import java.util.Map;

import static com.sbg.bdd.screenplay.core.actors.OnStage.shineSpotlightOn;
import static com.sbg.bdd.screenplay.core.actors.OnStage.theActorInTheSpotlight;

public class RestAssuredTasks {

    public static final String LAST_RESPONSE = "lastResponse";

    public static Task get(final String urii, final RequestSpecification spec) {
        return new Task() {
            String uri= getUriAndParams();

            private String getUriAndParams() {
                Map<String, String> queryParams = ((FilterableRequestSpecification) spec).getQueryParams();
                if(queryParams.isEmpty()) {
                    return urii;
                }else if(queryParams.size()==1){
                    return urii + " and parameter: " + queryParams;
                }else{
                    return urii + " and parameters: " + queryParams;
                }
            }

            @Override
            @Step("send a GET request to #uri")
            public <T extends Actor> T performAs(T actor) {
                ActorOnStage actorOnStage = shineSpotlightOn(actor);
                Response response = spec.config(RestAssuredConfig.config())
                        .filter(new CorrelationFilter()).get(urii);
                actorOnStage.remember(LAST_RESPONSE, response);
                return actor;
            }
        };
    }
    public static Task put(final String urii, final RequestSpecification spec) {
        return new Task() {
            String uri=urii;
            String body=((FilterableRequestSpecification)spec).getBody().toString();
            @Override
            @Step("send a PUT request to #uri with body '#body'")
            public <T extends Actor> T performAs(T actor) {
                ActorOnStage actorOnStage = shineSpotlightOn(actor);
                Response response = spec.config(RestAssuredConfig.config())
                        .filter(new CorrelationFilter())
                        .put(uri);
                actorOnStage.remember(LAST_RESPONSE, response);
                return actor;
            }
        };
    }
    public static Task post(final String urii, final RequestSpecification spec) {
        return new Task() {
            String uri=urii;
            String body=((FilterableRequestSpecification)spec).getBody().toString();
            @Override
            @Step("send a POST request to #uri with body '#body'")
            public <T extends Actor> T performAs(T actor) {
                ActorOnStage actorOnStage = shineSpotlightOn(actor);
                Response response = spec.config(RestAssuredConfig.config())
                        .filter(new CorrelationFilter()).post(uri);
                actorOnStage.remember(LAST_RESPONSE, response);
                return actor;
            }
        };
    }


    public static Task delete(final String urii, final RequestSpecification spec) {
        return new Task() {
            String uri=urii;
            @Override
            @Step("send a DELETE request to #uri")
            public <T extends Actor> T performAs(T actor) {
                ActorOnStage actorOnStage = shineSpotlightOn(actor);
                Response response = spec.config(RestAssuredConfig.config())
                        .filter(new CorrelationFilter()).delete(uri);
                actorOnStage.remember(LAST_RESPONSE, response);
                return actor;
            }
        };
    }

    public static void thenFor(Actor actor, ResponseConsequence... consequences) {
        actor.useKeyword("Then");
        actor.should(consequences);
    }
    public static <T> Question<T> bodyAs(final Class<T> clss){
        return new Question<T>() {
            @Override
            public T answeredBy(Actor actor) {
                ActorOnStage actorOnStage = actor.onStagePresence();
                Response response = actorOnStage.recall(LAST_RESPONSE);
                ValidatableResponse then = response.then();
                return then.extract().body().as(clss);
            }
        };


    }

    public static ValidatableResponse theLastResponse() {
        ActorOnStage actorOnStage = theActorInTheSpotlight();
        Response response = actorOnStage.recall(LAST_RESPONSE);
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
