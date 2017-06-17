package com.sbg.bdd.screenplay.restassured;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Question;
import com.sbg.bdd.screenplay.core.Task;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import static com.sbg.bdd.screenplay.core.actors.OnStage.theActorInTheSpotlight;
import static io.restassured.RestAssured.with;

public class RestAssuredTasks {
    public static interface HttpTask extends Task {
        HttpTask to(String uri);
    }

    public static final String LAST_RESPONSE = "lastResponse";

    public static Task get(final String uri, final RequestSpecification spec) {
        return new GetTask(uri, spec);
    }

    public static Task get(final String uri) {
        return new GetTask(uri);
    }

    public static HttpTask get() {
        return new GetTask();
    }

    public static Task put(final String uri, final RequestSpecification spec) {
        return new PutTask(uri, spec);
    }

    public static HttpTask  put(Object payload) {
        return new PutTask(with().body(payload));
    }

    public static Task post(final String uri, final RequestSpecification spec) {
        return new PostTask(uri, spec);
    }
    public static HttpTask post(Object payload) {
        return new PostTask(with().body(payload));
    }


    public static Task delete(final String urii, final RequestSpecification spec) {
        return new DeleteTask(urii, spec);
    }

    public static void thenFor(Actor actor, ResponseConsequence... consequences) {
        actor.useKeyword("Then");
        actor.should(consequences);
    }

    public static <T> Question<T> bodyAs(final Class<T> clss) {
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
