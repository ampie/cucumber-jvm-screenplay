package cucumber.scoping.wiremock.listeners;


import com.github.ampie.wiremock.admin.CorrelationState;
import cucumber.screenplay.annotations.*;
import cucumber.scoping.wiremock.CorrelationPath;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.Scene;
import cucumber.screenplay.events.StepEvent;
import cucumber.screenplay.internal.BaseActorOnStage;
import cucumber.wiremock.RecordingWireMockClient;

import static cucumber.screenplay.actors.OnStage.currentScene;
import static cucumber.screenplay.annotations.StepEventType.*;
import static cucumber.screenplay.annotations.StepEventType.STEP_FAILED;

public class ScopeManagementListener {
    @SceneListener(scopePhases = SceneEventType.BEFORE_START)
    public void registerScope(Scene scene) {
        String scopePath = CorrelationPath.of(scene);
        CorrelationState correlationState = getWireMock(scene).joinCorrelatedScope(scopePath);
        scene.remember("correlationState", correlationState);
    }

    @SceneListener(scopePhases = SceneEventType.AFTER_COMPLETE)
    public void unregisterScope(Scene scene) {
        String knownScopePath = CorrelationPath.of(scene);
        getWireMock(scene).stopCorrelatedScope(knownScopePath);
    }

    @StepListener(eventTypes = StepEventType.STEP_STARTED)
    public void registerStep(StepEvent event) {
        Scene scene = currentScene();
        getWireMock(scene).startStep(CorrelationPath.of(scene), event.getStepPath());
        ((CorrelationState)scene.recall("correlationState")).setCurrentStep(event.getStepPath());
    }

    @StepListener(eventTypes = {STEP_PENDING, STEP_SKIPPED, STEP_ASSERTION_FAILED, STEP_SUCCESSFUL, STEP_FAILED})
    public void unregisterStep(StepEvent stepEvent) {
        Scene scene = currentScene();
        getWireMock(scene).stopStep(CorrelationPath.of(scene), stepEvent.getStepPath());
        String stepPath = stepEvent.getStepPath();
        if(stepPath.lastIndexOf("/")>0){
            stepPath=stepPath.substring(0,stepPath.lastIndexOf("/"));
        }else{
            stepPath =null;
        }
        ((CorrelationState)scene.recall("correlationState")).setCurrentStep(stepPath);

    }


    @ActorListener(involvement = ActorInvolvement.BEFORE_ENTER_STAGE)
    public void registerScope(ActorOnStage userInScope) {
        if (!BaseActorOnStage.isEverybody(userInScope)) {
            String scopePath = CorrelationPath.of(userInScope);
            CorrelationState state = getWireMock(userInScope.getScene()).joinCorrelatedScope(scopePath);
            userInScope.remember("correlationState", state);
        }
    }
    private RecordingWireMockClient getWireMock(Scene scene) {
        return scene.getPerformance().recall("recordingWireMockClient");
    }



}
