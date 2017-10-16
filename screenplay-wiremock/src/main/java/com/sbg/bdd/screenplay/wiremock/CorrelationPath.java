package com.sbg.bdd.screenplay.wiremock;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.wiremock.scoped.admin.model.CorrelationState;
import com.sbg.bdd.wiremock.scoped.admin.model.GlobalCorrelationState;
import com.sbg.bdd.wiremock.scoped.client.ScopedWireMockClient;
import org.apache.commons.lang3.StringUtils;

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
        GlobalCorrelationState state = scope.getPerformance().recall(WireMockScreenplayContext.CORRELATION_STATE);
        //TODO simplify this
        if(state==null){
            return scope.getSceneIdentifier();
        }else if(StringUtils.isEmpty(scope.getSceneIdentifier())){
            return state.getCorrelationPath();
        }else{
            return state.getCorrelationPath() + "/" + scope.getSceneIdentifier();
        }
    }

}
