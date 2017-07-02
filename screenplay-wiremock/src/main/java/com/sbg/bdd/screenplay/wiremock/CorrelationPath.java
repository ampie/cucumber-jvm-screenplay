package com.sbg.bdd.screenplay.wiremock;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.wiremock.scoped.client.ScopedWireMockClient;

import static com.sbg.bdd.screenplay.core.util.NameConverter.filesystemSafe;

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
        StringBuilder path = new StringBuilder();
        WireMockMemories recall = WireMockMemories.recallFrom(scope);
        ScopedWireMockClient wireMock = recall.theWireMockClient();
        String publicAddress = recall.thePublicAddressOfWireMock();
        if (publicAddress == null) {
            path.append(wireMock.host());
        } else {
            path.append(publicAddress);
        }
        path.append("/").append(wireMock.port()).append("/").append(filesystemSafe(scope.getPerformance().getName()));
        Integer runId = recall.theRunId();
        if (runId != null) {
            path.append("/").append(runId);
        }
        if (!scope.getSceneIdentifier().isEmpty()) {
            path.append("/").append(scope.getSceneIdentifier());
        }
        return path.toString();
    }

}
