package cucumber.wiremock.scoping;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import cucumber.scoping.UserInScope;
import cucumber.scoping.UserTrackingScope;

public abstract class CorrelationPath {
    public static final String of(UserInScope scope) {
        return CorrelationPath.ofUserInScope(scope.getScope(),scope.getId());
    }

    public static final RegexPattern matching(UserTrackingScope scope, String suffix) {
        return (RegexPattern) WireMock.matching(CorrelationPath.of(scope) + suffix);
    }

    public static String ofUserInScope(UserTrackingScope scope, String userScopeId) {
        return of(scope) + "/"+ userScopeId;
    }

    public static String of(UserTrackingScope scope) {
        Integer runId = scope.getGlobalScope().getEverybodyScope().recall("runId");
        return (runId == null ? "" : runId.toString() + "/") + scope.getScopePath();
    }

}
