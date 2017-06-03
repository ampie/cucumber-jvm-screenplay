package cucumber.wiremock.listeners;


import com.github.tomakehurst.wiremock.common.Json;
import com.sbg.bdd.cucumber.screenplay.core.formatter.ScreenPlayFormatter;
import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.annotations.Within;
import com.sbg.bdd.screenplay.scoped.ScenarioScope;
import com.sbg.bdd.screenplay.scoped.StepScope;
import com.sbg.bdd.screenplay.scoped.VerificationScope;
import com.sbg.bdd.screenplay.wiremock.CorrelationPath;
import com.sbg.bdd.screenplay.wiremock.listeners.ScopeManagementListener;
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedExchange;
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient;

import java.util.List;

import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;

@Within(ScopeManagementListener.class)
public class ExchangeLoggingListener {


    @StepListener(eventTypes = {SUCCESSFUL, SKIPPED, PENDING, ASSERTION_FAILED, FAILED})
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
