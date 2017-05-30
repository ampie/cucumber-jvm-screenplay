package com.sbg.bdd.screenplay.wiremock;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;

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
        String base = (runId == null ? "" : runId.toString() + "/") + scope.getPerformance().getName();
        if(scope.getSceneIdentifier().isEmpty()){
            return base;
        }else {
            return base + "/" + scope.getSceneIdentifier();
        }
    }

}