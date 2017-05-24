package cucumber.scoping.wiremock;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.Scene;

public abstract class CorrelationPath {
    public static final String of(ActorOnStage actorOnStage) {
        return CorrelationPath.ofUserInScope(actorOnStage.getScene(),actorOnStage.getId());
    }

    public static final RegexPattern matching(Scene scope, String suffix) {
        return (RegexPattern) WireMock.matching(CorrelationPath.of(scope) + suffix);
    }

    public static String ofUserInScope(Scene scope, String userScopeId) {
        return of(scope) + "/"+ userScopeId;
    }

    public static String of(Scene scope) {
        Integer runId = scope.getPerformance().recall("runId");
        return (runId == null ? "" : runId.toString() + "/") + scope.getIdentifier();
    }

}
