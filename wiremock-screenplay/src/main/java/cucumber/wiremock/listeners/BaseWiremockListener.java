package cucumber.wiremock.listeners;

import cucumber.scoping.VerificationScope;
import cucumber.wiremock.RecordingWireMockClient;


public class BaseWiremockListener {
    public String getScopePath(VerificationScope scope) {
        Integer runId = scope.getGlobalScope().recall("runId");
        return runId + "/" + scope.getScopePath();
    }

    public RecordingWireMockClient getWireMock(VerificationScope scope) {
        return scope.getGlobalScope().recall(RecordingWireMockClient.class);
    }
}
