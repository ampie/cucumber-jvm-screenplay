package cucumber.wiremock;

import java.io.File;
import java.net.URL;
import java.util.Map;


public interface WireMockContext extends EndpointPropertyResolver {
    Map<String, String> allKnownExternalEndpoints();

    URL endpointUrlFor(String serviceEndpointPropertyName);

    File resolveResource(String fileName);

    String getBaseUrlOfServiceUnderTest();

    void register(WireMockRuleBuilder child);

    int count(ExtendedRequestPatternBuilder requestPatternBuilder);

    Integer calculatePriority(int localLevel);
}
