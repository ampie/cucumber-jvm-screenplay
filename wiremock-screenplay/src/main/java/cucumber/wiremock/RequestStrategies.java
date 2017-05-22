package cucumber.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

public class RequestStrategies {
    public static  StringValuePattern[] containing(String... strings) {
        StringValuePattern[] result = new StringValuePattern[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = WireMock.containing(strings[i]);
        }
        return result;
    }
    public static  ExtendedRequestPatternBuilder aGet(){
        return a(RequestMethod.GET);
    }
    public static  ExtendedRequestPatternBuilder aPost(){
        return a(RequestMethod.POST);
    }
    public static  ExtendedRequestPatternBuilder aPut(){
        return a(RequestMethod.PUT);
    }
    public static  ExtendedRequestPatternBuilder aDelete(){
        return a(RequestMethod.DELETE);
    }
    public static  ExtendedRequestPatternBuilder a(RequestMethod method){
        return a(method.getName());
    }
    public static  ExtendedRequestPatternBuilder a(String method) {
        ExtendedRequestPatternBuilder extendedMappingBuilder = new ExtendedRequestPatternBuilder(RequestMethod.fromString(method));
        return extendedMappingBuilder;
    }

    public static  ExtendedRequestPatternBuilder anyRequest() {
        return a("ANY");
    }

    public static  ExtendedRequestPatternBuilder allRequests() {
        return a("ANY");
    }
}
