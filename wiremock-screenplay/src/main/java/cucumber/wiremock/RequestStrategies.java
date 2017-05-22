package cucumber.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

public abstract class RequestStrategies {
    public static  StringValuePattern[] containing(String... strings) {
        StringValuePattern[] result = new StringValuePattern[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = WireMock.containing(strings[i]);
        }
        return result;
    }

    public static  ExtendedRequestPatternBuilder a(RequestMethod method){
        return a(method.getName());
    }
    public static  ExtendedRequestPatternBuilder a(String method) {
        ExtendedRequestPatternBuilder extendedMappingBuilder = new ExtendedRequestPatternBuilder(RequestMethod.fromString(method));
        return extendedMappingBuilder;
    }

    public static  ExtendedRequestPatternBuilder anyRequest() {
        return allRequests();
    }

    public static  ExtendedRequestPatternBuilder allRequests() {
        return a("ANY");
    }
}
