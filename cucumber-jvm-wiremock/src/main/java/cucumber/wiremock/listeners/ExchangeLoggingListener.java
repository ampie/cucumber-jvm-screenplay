package cucumber.wiremock.listeners;


import com.github.ampie.wiremock.RecordedExchange;
import com.github.tomakehurst.wiremock.common.Json;
import cucumber.scoping.ScenarioScope;
import cucumber.scoping.StepScope;
import cucumber.scoping.VerificationScope;
import cucumber.screenplay.annotations.SceneEventType;
import cucumber.screenplay.annotations.SceneListener;
import cucumber.screenplay.formatter.ScreenPlayFormatter;
import cucumber.wiremock.RecordingWireMockClient;
import cucumber.scoping.wiremock.CorrelationPath;

import java.util.List;

public class ExchangeLoggingListener {


    @SceneListener(scopePhases = SceneEventType.BEFORE_COMPLETE)
    public void logExchanges(StepScope scope) {
        ScenarioScope scenarioScope = scope.getNearestContaining(ScenarioScope.class);
        String scopePath = CorrelationPath.of(scenarioScope);
        List<RecordedExchange> exchanges = getWireMock(scope).findExchangesAgainstStep(scopePath, scope.getStepPath());
        ScreenPlayFormatter.getCurrent().embedding("application/json", Json.write(exchanges).getBytes());
    }

    public RecordingWireMockClient getWireMock(VerificationScope scope) {
        return scope.getGlobalScope().getEverybodyScope().recall(RecordingWireMockClient.class);
    }
}
