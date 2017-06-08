package com.sbg.bdd.screenplay.restassured;

import com.sbg.bdd.screenplay.core.*;
import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.screenplay.core.annotations.Subject;
import com.sbg.bdd.screenplay.core.util.Fields;
import io.restassured.assertion.BodyMatcher;
import io.restassured.assertion.BodyMatcherGroup;
import io.restassured.assertion.HeaderMatcher;
import io.restassured.function.RestAssuredFunction;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.sbg.bdd.screenplay.core.actors.OnStage.callActorToStage;

public class ResponseConsequence extends BaseConsequence implements Consequence, ResponseSpecification {
    private ResponseSpecification specification;

    public ResponseConsequence(ResponseSpecification specification) {
        this.specification = specification;
    }


    @Override
    public void evaluateFor(Actor actor) {
        ActorOnStage actorOnStage = callActorToStage(actor);
        Response response = actorOnStage.recall("lastResponse");
        specification.validate(response);

    }
    @Override
    public String toString() {
        Map<String, Object> specFields = Fields.of(specification).asMap();
        Description description = new StringDescription();
        description.appendText("see that ");
        boolean hasAdded=false;
        if(specFields.get("bodyMatchers")!=null){
            description.appendText("the body of the response ");
            BodyMatcherGroup bmg = (BodyMatcherGroup) specFields.get("bodyMatchers");
            Collection<BodyMatcher> bodyAssertions = (Collection<BodyMatcher>) Fields.of(bmg).asMap().get("bodyAssertions");
            for (BodyMatcher matcher : bodyAssertions) {
                matcher.getMatcher().describeTo(description);
            }
            hasAdded=true;
        }
        if(specFields.get("headerAssertions")!=null){
            Collection<HeaderMatcher> headerAssertions = (Collection<HeaderMatcher>) specFields.get("headerAssertions");
            for (HeaderMatcher assertion : headerAssertions) {
                if(hasAdded) {
                    description.appendText("and ");
                }
                hasAdded=true;
                description.appendText("the header \"");
                description.appendText(assertion.getHeaderName().toString());
                description.appendText("\" ");
                assertion.getMatcher().describeTo(description);
            }
        }
        if(specFields.get("expectedStatusCode")!=null){
            Matcher<Integer> expectedStatusCode= (Matcher<Integer>) specFields.get("expectedStatusCode");
            if(hasAdded) {
                description.appendText("and ");
            }
            hasAdded=true;
            description.appendText("the status code ");
            expectedStatusCode.describeTo(description);
        }
        //TODO cookies assertions
        return description.toString();
    }


    @Override
    public Question getQuestion() {
        return new Question() {
            @Override
            @Subject("the response")
            public Object answeredBy(Actor actor) {
                return specification.toString();
            }
        };
    }

    @Override
    public ResponseConsequence spec(ResponseSpecification responseSpecificationToMerge) {
        specification.spec(responseSpecificationToMerge);
        return this;
    }
    
    @Override
    @Deprecated
    public ResponseConsequence specification(ResponseSpecification responseSpecificationToMerge) {
        specification.specification(responseSpecificationToMerge);
        return this;
    }
    
    @Override
    public ResponseConsequence parser(String contentType, Parser parser) {
        specification.parser(contentType, parser);
        return this;
    }
    
    @Override
    public ResponseConsequence defaultParser(Parser parser) {
        specification.defaultParser(parser);
        return this;
    }
    
    @Override
    @Deprecated
    public ResponseConsequence content(Matcher<?> matcher, Matcher<?>... additionalMatchers) {
        specification.content(matcher, additionalMatchers);
        return this;
    }
    
    @Override
    @Deprecated
    public ResponseConsequence content(List<Argument> arguments, Matcher matcher, Object... additionalKeyMatcherPairs) {
        specification.content(arguments, matcher, additionalKeyMatcherPairs);
        return this;
    }
    
    @Override
    public Response validate(Response response) {
        return specification.validate(response);
    }
    
    @Override
    @Deprecated
    public ResponseConsequence content(String key, Matcher<?> matcher, Object... additionalKeyMatcherPairs) {
        specification.content(key, matcher, additionalKeyMatcherPairs);
        return this;
    }
    
    @Override
    public ResponseConsequence time(Matcher<Long> matcher) {
        specification.time(matcher);
        return this;
    }
    
    @Override
    public ResponseConsequence time(Matcher<Long> matcher, TimeUnit timeUnit) {
        specification.time(matcher, timeUnit);
        return this;
    }
    
    @Override
    public ResponseConsequence body(String key, List<Argument> arguments, Matcher matcher, Object... additionalKeyMatcherPairs) {
        specification.body(key, arguments, matcher, additionalKeyMatcherPairs);
        return this;
    }
    
    @Override
    public ResponseConsequence body(List<Argument> arguments, Matcher matcher, Object... additionalKeyMatcherPairs) {
        specification.body(arguments, matcher, additionalKeyMatcherPairs);
        return this;
    }
    
    @Override
    public ResponseConsequence statusCode(Matcher<? super Integer> expectedStatusCode) {
        specification.statusCode(expectedStatusCode);
        return this;
    }
    
    @Override
    public ResponseConsequence statusCode(int expectedStatusCode) {
        specification.statusCode(expectedStatusCode);
        return this;
    }
    
    @Override
    public ResponseConsequence statusLine(Matcher<? super String> expectedStatusLine) {
        specification.statusLine(expectedStatusLine);
        return this;
    }
    
    @Override
    public ResponseConsequence statusLine(String expectedStatusLine) {
        specification.statusLine(expectedStatusLine);
        return this;
    }
    
    @Override
    public ResponseConsequence headers(Map<String, ?> expectedHeaders) {
        specification.headers(expectedHeaders);
        return this;
    }
    
    @Override
    public ResponseConsequence headers(String firstExpectedHeaderName, Object firstExpectedHeaderValue, Object... expectedHeaders) {
        specification.headers(firstExpectedHeaderName, firstExpectedHeaderValue, expectedHeaders);
        return this;
    }
    
    @Override
    public ResponseConsequence header(String headerName, Matcher<?> expectedValueMatcher) {
        specification.header(headerName, expectedValueMatcher);
        return this;
    }
    
    @Override
    public <T> ResponseSpecification header(String headerName, RestAssuredFunction<String, T> mappingFunction, Matcher<? super T> expectedValueMatcher) {
        specification.header(headerName, mappingFunction, expectedValueMatcher);
        return this;
    }
    
    @Override
    public ResponseConsequence header(String headerName, String expectedValue) {
        specification.header(headerName, expectedValue);
        return this;
    }
    
    @Override
    public ResponseConsequence cookies(Map<String, ?> expectedCookies) {
        specification.cookies(expectedCookies);
        return this;
    }
    
    @Override
    public ResponseConsequence cookie(String cookieName) {
        specification.cookie(cookieName);
        return this;
    }
    
    @Override
    public ResponseConsequence cookies(String firstExpectedCookieName, Object firstExpectedCookieValue, Object... expectedCookieNameValuePairs) {
        specification.cookies(firstExpectedCookieName, firstExpectedCookieValue, expectedCookieNameValuePairs);
        return this;
    }
    
    @Override
    public ResponseConsequence cookie(String cookieName, Matcher<?> expectedValueMatcher) {
        specification.cookie(cookieName, expectedValueMatcher);
        return this;
    }
    
    @Override
    public ResponseConsequence cookie(String cookieName, Object expectedValue) {
        specification.cookie(cookieName, expectedValue);
        return this;
    }
    
    @Override
    public ResponseLogSpecification log() {
        return specification.log();
    }
    
    @Override
    @Deprecated
    public ResponseConsequence rootPath(String rootPath) {
        specification.rootPath(rootPath);
        return this;
    }
    
    @Override
    @Deprecated
    public ResponseConsequence rootPath(String rootPath, List<Argument> arguments) {
        specification.rootPath(rootPath, arguments);
        return this;
    }
    
    @Override
    public ResponseConsequence root(String rootPath, List<Argument> arguments) {
        specification.root(rootPath, arguments);
        return this;
    }
    
    @Override
    public ResponseConsequence root(String rootPath) {
        specification.root(rootPath);
        return this;
    }
    
    @Override
    public ResponseConsequence noRoot() {
        specification.noRoot();
        return this;
    }
    
    @Override
    @Deprecated
    public ResponseConsequence noRootPath() {
        specification.noRootPath();
        return this;
    }
    
    @Override
    public ResponseConsequence appendRoot(String pathToAppend) {
        specification.appendRoot(pathToAppend);
        return this;
    }
    
    @Override
    public ResponseConsequence appendRoot(String pathToAppend, List<Argument> arguments) {
        specification.appendRoot(pathToAppend, arguments);
        return this;
    }
    
    @Override
    public ResponseConsequence detachRoot(String pathToDetach) {
        specification.detachRoot(pathToDetach);
        return this;
    }
    
    @Override
    public ResponseConsequence contentType(ContentType contentType) {
        specification.contentType(contentType);
        return this;
    }
    
    @Override
    public ResponseConsequence contentType(String contentType) {
        specification.contentType(contentType);
        return this;
    }
    
    @Override
    public ResponseConsequence contentType(Matcher<? super String> contentType) {
        specification.contentType(contentType);
        return this;
    }
    
    @Override
    public ResponseConsequence body(Matcher<?> matcher, Matcher<?>... additionalMatchers) {
        specification.body(matcher, additionalMatchers);
        return this;
    }
    
    @Override
    public ResponseConsequence body(String path, Matcher<?> matcher, Object... additionalKeyMatcherPairs) {
        specification.body(path, matcher, additionalKeyMatcherPairs);
        return this;
    }
    
    @Override
    @Deprecated
    public ResponseConsequence content(String path, List<Argument> arguments, Matcher matcher, Object... additionalKeyMatcherPairs) {
        specification.content(path, arguments, matcher, additionalKeyMatcherPairs);
        return this;
    }
    
    @Override
    public RequestSender when() {
        return specification.when();
    }
    
    @Override
    public RequestSpecification given() {
        return specification.given();
    }
    
    @Override
    public ResponseConsequence that() {
        specification.that();
        return this;
    }
    
    @Override
    public RequestSpecification request() {
        return specification.request();
    }
    
    @Override
    public ResponseConsequence response() {
        specification.response();
        return this;
    }
    
    @Override
    public ResponseConsequence and() {
        specification.and();
        return this;
    }
    
    @Override
    public RequestSpecification with() {
        return specification.with();
    }
    
    @Override
    public ResponseConsequence then() {
        specification.then();
        return this;
    }

    @Override
    public ResponseSpecification expect() {
        specification.expect();
        return this;
    }
}
