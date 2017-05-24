package cucumber.scoping.events;

import cucumber.scoping.GlobalScope;
import cucumber.scoping.ScenarioScope;
import cucumber.scoping.StepScope;
import cucumber.screenplay.actors.OnStage;
import cucumber.screenplay.annotations.StepListener;
import cucumber.screenplay.events.StepEvent;

import static cucumber.screenplay.annotations.StepEventType.*;

public class ScreenplayLifecycleSync {

    @StepListener(eventTypes = STEP_STARTED)
    public void startStep(StepEvent event) {
        StepScope innerMostActive = getGlobalScope().getInnerMostActive(StepScope.class);
        if (innerMostActive == null) {
            ScenarioScope scenarioScope = getGlobalScope().getInnerMostActive(ScenarioScope.class);
            scenarioScope.startStep(event.getInfo().getName());
        } else {
            innerMostActive.startChildStep(event.getInfo().getName());
        }
    }

    private GlobalScope getGlobalScope() {
        return (GlobalScope) OnStage.performance();
    }

    @StepListener(eventTypes = {STEP_PENDING, STEP_SKIPPED, STEP_ASSERTION_FAILED, STEP_SUCCESSFUL, STEP_FAILED})
    public void finishStep(StepEvent event) {
        StepScope stepScope = getGlobalScope().getInnerMostActive(StepScope.class);

        stepScope.getContainingScope().completeNestedScope(event.getInfo().getName());

    }

}
