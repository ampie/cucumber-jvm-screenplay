package cucumber.wiremock.listeners;

import com.sbg.bdd.cucumber.screenplay.scoped.plugin.CucumberPayloadProducingListener;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement;
import com.sbg.bdd.screenplay.core.annotations.ActorListener;
import com.sbg.bdd.screenplay.core.annotations.SceneEventType;
import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.core.internal.BaseActorOnStage;
import com.sbg.bdd.screenplay.scoped.FunctionalScope;
import com.sbg.bdd.screenplay.scoped.ScenarioScope;
import com.sbg.bdd.screenplay.wiremock.CorrelationPath;
import com.sbg.bdd.screenplay.wiremock.WireMockScreenplayContext;
import com.sbg.bdd.wiremock.scoped.admin.model.CorrelationState;
import com.sbg.bdd.wiremock.scoped.common.ParentPath;
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory;
import com.sbg.bdd.wiremock.scoped.integration.WireMockCorrelationState;
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient;

import java.util.Collections;
import java.util.Map;

import static com.sbg.bdd.screenplay.core.actors.OnStage.theCurrentScene;

public class ScopeManagementListener extends CucumberPayloadProducingListener {


    @SceneListener(scopePhases = SceneEventType.AFTER_COMPLETE)
    public void unregisterScope(Scene scene) {
        String knownScopePath = CorrelationPath.of(scene);
        getWireMockFrom(scene).stopCorrelatedScope(knownScopePath);
    }

    @Override
    protected void scopeStarted(Scene scene) {
        registerScope(scene, Collections.<String, Object>emptyMap());
    }

    @Override
    protected void featureStarted(FunctionalScope scene, Map<String, Object> map) {
        registerScope(scene, map);
    }

    private void registerScope(Scene scene, Map<String, Object> map) {
        RecordingWireMockClient wireMock = getWireMockFrom(scene);
        String scopePath = CorrelationPath.of(scene);
        CorrelationState correlationState;
        if (scene.getLevel() == 0) {
            correlationState = wireMock.startNewCorrelatedScope(scopePath);
            scene.remember("runId", Integer.valueOf(correlationState.getCorrelationPath().split("/")[3]));
        } else {
            correlationState = wireMock.joinCorrelatedScope(scopePath, map);
        }
        scene.remember(WireMockScreenplayContext.CORRELATION_STATE, correlationState);
    }

    @Override
    protected void scenarioPhaseEntered(ScenarioScope scene, Map<String, Object> payload) {
        registerScope(scene, payload);
    }

    @Override
    protected void stepStarted(StepEvent event, Map<String, Object> payload) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).startStep(CorrelationPath.of(theCurrentScene), event.getStepPath(), payload);
        ((CorrelationState) theCurrentScene.recall(WireMockScreenplayContext.CORRELATION_STATE)).setCurrentStep(event.getStepPath());
    }

    @Override
    protected void stepCompleted(StepEvent event, Map<String, Object> payload) {
        Scene theCurrentScene = theCurrentScene();
        getWireMockFrom(theCurrentScene).stopStep(CorrelationPath.of(theCurrentScene), event.getStepPath(), payload);
        String stepPath = ParentPath.of(event.getStepPath());
        ((CorrelationState) theCurrentScene.recall(WireMockScreenplayContext.CORRELATION_STATE)).setCurrentStep(stepPath);
    }


    @ActorListener(involvement = ActorInvolvement.INTO_SPOTLIGHT)
    public void syncCorrelationState(ActorOnStage userInScope) {
        if (!BaseActorOnStage.isEverybody(userInScope)) {
            CorrelationState state = userInScope.recall(WireMockScreenplayContext.CORRELATION_STATE);
            WireMockCorrelationState currentCorrelationState = DependencyInjectionAdaptorFactory.getAdaptor().getCurrentCorrelationState();
            currentCorrelationState.clear();
            currentCorrelationState.set(state.getCorrelationPath(), Boolean.TRUE.equals(userInScope.recall(WireMockScreenplayContext.PROXY_UNMAPPED_ENDPOINTS)));
            for (Map.Entry<String, Integer> entry : state.getServiceInvocationCounts().entrySet()) {
                currentCorrelationState.initSequenceNumberFor(entry.getKey(), entry.getValue());
            }
        }
    }

    @ActorListener(involvement = ActorInvolvement.BEFORE_ENTER_STAGE)
    public void registerScope(ActorOnStage userInScope) {
        if (!BaseActorOnStage.isEverybody(userInScope)) {
            String scopePath = CorrelationPath.of(userInScope);
            CorrelationState state = getWireMockFrom(userInScope.getScene()).joinCorrelatedScope(scopePath);
            userInScope.remember(WireMockScreenplayContext.CORRELATION_STATE, state);
        }
    }

    private RecordingWireMockClient getWireMockFrom(Scene scene) {
        return scene.getPerformance().recall(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT);
    }
}
