package com.sbg.bdd.screenplay.wiremock;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient;

public abstract class CorrelationPath {
    public static final String of(ActorOnStage actorOnStage) {
        return ofUserInScope(actorOnStage.getScene(), actorOnStage.getId());
    }

    public static final RegexPattern matching(Scene scope, String suffix) {
        return (RegexPattern) WireMock.matching(CorrelationPath.of(scope) + suffix);
    }

    public static String ofUserInScope(Scene scope, String userScopeId) {
        return of(scope) + "/" + userScopeId;
    }

    public static String of(Scene scope) {
        Integer runId = scope.getPerformance().recall("runId");
        RecordingWireMockClient wireMock = scope.getPerformance().recall("recordingWireMockClient");
        String hostPrefix = wireMock.host() + "/" + wireMock.port() + "/";
        String base = (runId == null ? "" : runId.toString() + "/") + scope.getPerformance().getName();
        if (scope.getSceneIdentifier().isEmpty()) {
            return hostPrefix + base;
        } else {
            return hostPrefix + base + "/" + scope.getSceneIdentifier();
        }
    }

}
