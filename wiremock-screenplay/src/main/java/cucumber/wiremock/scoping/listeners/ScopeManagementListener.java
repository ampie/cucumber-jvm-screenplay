package cucumber.wiremock.scoping.listeners;


import com.github.ampie.wiremock.admin.CorrelationState;
import cucumber.scoping.*;
import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.annotations.SubscribeToScope;
import cucumber.scoping.annotations.SubscribeToUser;
import cucumber.scoping.annotations.UserInvolvement;
import cucumber.wiremock.RecordingWireMockClient;
import cucumber.wiremock.scoping.CorrelationPath;

public class ScopeManagementListener {
    @SubscribeToScope(scopePhases = ScopePhase.BEFORE_START)
    public void registerScope(UserTrackingScope scope) {
        String scopePath = CorrelationPath.of(scope);
        CorrelationState correlationState = getWireMock(scope).joinCorrelatedScope(scopePath);
        scope.getEverybodyScope().remember(correlationState);
    }

    public RecordingWireMockClient getWireMock(VerificationScope scope) {
        return scope.getGlobalScope().getEverybodyScope(). recall(RecordingWireMockClient.class);
    }

    @SubscribeToScope(scopePhases = ScopePhase.BEFORE_START)
    public void registerStep(StepScope scope) {
        ScenarioScope scenarioScope = scope.getNearestContaining(ScenarioScope.class);
        getWireMock(scope).startStep(CorrelationPath.of(scenarioScope), scope.getStepPath());
        scope.getNearestContaining(ScenarioScope.class).getEverybodyScope().recall(CorrelationState.class).setCurrentStep(scope.getStepPath());
    }

    @SubscribeToUser(involvement = UserInvolvement.BEFORE_ENTER_STAGE)
    public void registerScope(UserInScope userInScope) {
        if (userInScope instanceof GuestInScope || userInScope instanceof ActorInScope) {
            String scopePath = CorrelationPath.of(userInScope);
            CorrelationState state = getWireMock(userInScope.getScope()).joinCorrelatedScope(scopePath);
            userInScope.remember(state);
        }
    }

    @SubscribeToScope(scopePhases = ScopePhase.AFTER_COMPLETE)
    public void unregisterScope(UserTrackingScope scope) {
        String knownScopePath = CorrelationPath.of(scope);
        getWireMock(scope).stopCorrelatedScope(knownScopePath);
    }

    @SubscribeToScope(scopePhases = ScopePhase.AFTER_COMPLETE)
    public void unregisterStep(StepScope scope) {
        ScenarioScope scenarioScope = scope.getNearestContaining(ScenarioScope.class);
        getWireMock(scope).stopStep(CorrelationPath.of(scenarioScope), scope.getStepPath());
        if (scope.getContainingScope() instanceof StepScope) {
            scope.getNearestContaining(ScenarioScope.class).getEverybodyScope().recall(CorrelationState.class).setCurrentStep(((StepScope) scope.getContainingScope()).getStepPath());
        } else {
            scope.getNearestContaining(ScenarioScope.class).getEverybodyScope().recall(CorrelationState.class).setCurrentStep(null);
        }
    }

}
