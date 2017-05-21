package cucumber.wiremock.listeners;


import com.github.ampie.wiremock.RecordedExchange;
import com.github.tomakehurst.wiremock.common.Json;
import cucumber.scoping.ScenarioScope;
import cucumber.scoping.StepScope;
import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.annotations.SubscribeToScope;
import cucumber.screenplay.formatter.ScreenPlayFormatter;

import java.util.List;

public class ExchangeLoggingListener extends BaseWiremockListener {


    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_COMPLETE)
    public void logExchanges(StepScope scope) {
        ScenarioScope scenarioScope = scope.getNearestContaining(ScenarioScope.class);
        String scopePath = getScopePath(scenarioScope);
        List<RecordedExchange> exchanges = getWireMock(scope).findExchangesAgainstStep(scopePath, scope.getStepPath());
        ScreenPlayFormatter.getCurrent().embedding("application/json", Json.write(exchanges).getBytes());
    }
}
