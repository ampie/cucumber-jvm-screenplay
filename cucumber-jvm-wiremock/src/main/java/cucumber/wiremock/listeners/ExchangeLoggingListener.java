package cucumber.wiremock.listeners;


import com.github.tomakehurst.wiremock.common.Json;
import com.sbg.bdd.cucumber.screenplay.core.formatter.ScreenPlayFormatter;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.annotations.Within;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.scoped.ScenarioScope;
import com.sbg.bdd.screenplay.scoped.StepScope;
import com.sbg.bdd.screenplay.scoped.VerificationScope;
import com.sbg.bdd.screenplay.wiremock.CorrelationPath;
import com.sbg.bdd.screenplay.wiremock.WireMockScreenplayContext;
import com.sbg.bdd.screenplay.wiremock.listeners.ScopeManagementListener;
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedExchange;
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient;

import java.util.List;

import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;

@Within(ScopeManagementListener.class)
public class ExchangeLoggingListener {


    @StepListener(eventTypes = {SUCCESSFUL, SKIPPED, PENDING, ASSERTION_FAILED, FAILED})
    public void logExchanges(StepEvent event) {
        Scene scenarioScope = OnStage.theCurrentScene();
        String scopePath = CorrelationPath.of(scenarioScope);
        List<RecordedExchange> exchanges = getWireMock(scenarioScope).findExchangesAgainstStep(scopePath, event.getStepPath());
        ScreenPlayFormatter.getCurrent().embedding("application/json", Json.write(exchanges).getBytes());
    }

    public RecordingWireMockClient getWireMock(Scene scope) {
        return scope.recall(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT);
    }
}
