package com.sbg.bdd.screenplay.restassured;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.annotations.Step;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static com.sbg.bdd.screenplay.core.actors.OnStage.shineSpotlightOn;
import static com.sbg.bdd.screenplay.restassured.PutTask.prependBaseUrl;
import static io.restassured.RestAssured.with;

public class GetTask implements RestAssuredTasks.HttpTask {
    private String uri;
    private RequestSpecification spec;
    private String descriptiveUri;

    public GetTask(String uri) {
        this(uri,with());

    }
    public GetTask(String uri, RequestSpecification spec) {
        this.uri = uri;
        this.spec = spec;
        descriptiveUri = getUriAndParams(uri);
    }

    public GetTask() {
        this.spec = with();
    }

    @Override
    public RestAssuredTasks.HttpTask to(String urii) {
        this.descriptiveUri =getUriAndParams(urii);
        return this;
    }


    private String getUriAndParams(String urii) {
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
    @Step("send a GET request to #descriptiveUri")
    public <T extends Actor> T performAs(T actor) {
        ActorOnStage actorOnStage = shineSpotlightOn(actor);
        prependBaseUrl(actorOnStage, uri, spec);

        Response response = spec.filter(new CorrelationFilter()).get(uri);
        actorOnStage.remember(RestAssuredTasks.LAST_RESPONSE, response);
        return actor;
    }
}
