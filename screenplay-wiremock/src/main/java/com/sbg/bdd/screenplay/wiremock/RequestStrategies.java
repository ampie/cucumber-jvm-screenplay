package com.sbg.bdd.screenplay.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.sbg.bdd.wiremock.scoped.client.junit.InMemoryWireMockContext;

public abstract class RequestStrategies {
    public static  StringValuePattern[] containing(String... strings) {
        StringValuePattern[] result = new StringValuePattern[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = WireMock.containing(strings[i]);
        }
        return result;
    }

    public static ScreenPlayRequestPatternBuilder a(RequestMethod method){
        return a(method.getName());
    }
    public static ScreenPlayRequestPatternBuilder a(String method) {
        ScreenPlayRequestPatternBuilder extendedMappingBuilder = new ScreenPlayRequestPatternBuilder(RequestMethod.fromString(method));
        return extendedMappingBuilder;
    }

    /**
     * For use outside of a Screenplay context
     * @param builder
     */
    public static void stubFor(ScreenPlayMappingBuilder builder) {
        builder.applyTo(new InMemoryWireMockContext());
    }
    public static ScreenPlayRequestPatternBuilder anyRequest() {
        return allRequests();
    }

    public static ScreenPlayRequestPatternBuilder allRequests() {
        return a("ANY");
    }
}
