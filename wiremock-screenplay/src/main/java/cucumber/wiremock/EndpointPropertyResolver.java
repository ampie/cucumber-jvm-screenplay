package cucumber.wiremock;

import java.net.URL;

public interface EndpointPropertyResolver {
    URL endpointUrlFor(String propertyName);
}
