package cucumber.wiremock;

import java.net.URL;
import java.util.Map;

public interface ClientOfServiceUnderTest {
    URL endpointUrlFor(String serviceEndpointPropertyName);


    Map<String, String> allKnowExternalEndpoints();

}
