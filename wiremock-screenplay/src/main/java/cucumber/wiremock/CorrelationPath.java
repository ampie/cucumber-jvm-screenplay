package cucumber.wiremock;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import cucumber.scoping.UserTrackingScope;

public abstract class CorrelationPath {
    public static final RegexPattern matching(UserTrackingScope scope, String suffix) {
        Integer runId = scope.getGlobalScope().recall("runId");
        String preFix = runId == null ? "" : runId.toString() + "/";
        String pattern = preFix + scope.getScopePath() + suffix;
        return (RegexPattern) WireMock.matching(pattern);
    }

    public static final EqualToPattern equalTo(UserTrackingScope scope, String suffix) {
        Integer runId = scope.getGlobalScope().recall("runId");
        String preFix = runId == null ? "" : runId.toString() + "/";
        String pattern = preFix + scope.getScopePath() + suffix;
        return (EqualToPattern) WireMock.equalTo(pattern);
    }
}
