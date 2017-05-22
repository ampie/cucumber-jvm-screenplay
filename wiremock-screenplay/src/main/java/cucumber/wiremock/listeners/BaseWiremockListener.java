package cucumber.wiremock.listeners;

import cucumber.scoping.UserTrackingScope;
import cucumber.scoping.VerificationScope;
import cucumber.wiremock.CorrelationPath;
import cucumber.wiremock.RecordingWireMockClient;


public class BaseWiremockListener {
    public String getScopePath(UserTrackingScope scope) {
        return CorrelationPath.equalTo(scope,"").getEqualTo();
    }

    public RecordingWireMockClient getWireMock(VerificationScope scope) {
        return scope.getGlobalScope().recall(RecordingWireMockClient.class);
    }
}
