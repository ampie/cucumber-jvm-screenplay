package cucumber.wiremock.listeners;


import com.github.ampie.wiremock.admin.CorrelationState;
import cucumber.scoping.*;
import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.annotations.SubscribeToScope;
import cucumber.scoping.annotations.SubscribeToUser;
import cucumber.scoping.annotations.UserInvolvement;
import cucumber.wiremock.RecordingWireMockClient;

public class ScopeManagementListener extends BaseWiremockListener {
    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_START)
    public void registerScope(UserTrackingScope scope) {
        String scopePath = getScopePath(scope);
        CorrelationState correlationState = getWireMock(scope).joinCorrelatedScope(scopePath);
        scope.getEverybodyScope().remember(correlationState);
    }

    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_START)
    public void registerScope(StepScope scope) {
        ScenarioScope scenarioScope = scope.getNearestContaining(ScenarioScope.class);
        getWireMock(scope).startStep(getScopePath(scenarioScope), scope.getStepPath());
    }

    @SubscribeToUser(involvement = UserInvolvement.BEFORE_ENTER)
    public void registerScope(UserInScope userInScope) {
        if (userInScope instanceof GuestInScope || userInScope instanceof ActorInScope) {
            String scopePath = getScopePath(userInScope.getScope());
            getWireMock(userInScope.getScope()).joinCorrelatedScope(scopePath + "/" + userInScope.getId());
        }
    }

    @SubscribeToScope(scopePhase = ScopePhase.AFTER_COMPLETE)
    public void unregisterScope(UserTrackingScope scope) {
        String knownScopePath = getScopePath(scope);
        getWireMock(scope).stopCorrelatedScope(knownScopePath);
    }

    @SubscribeToScope(scopePhase = ScopePhase.AFTER_COMPLETE)
    public void unregisterScope(StepScope scope) {
        ScenarioScope scenarioScope = scope.getNearestContaining(ScenarioScope.class);
        getWireMock(scope).stopStep(getScopePath(scenarioScope), scope.getStepPath());
    }

}
