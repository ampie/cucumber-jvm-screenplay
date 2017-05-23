package cucumber.scoping.events;

import cucumber.scoping.GlobalScope;
import cucumber.scoping.StepScope;
import cucumber.screenplay.actors.OnStage;
import cucumber.screenplay.annotations.StepListener;
import cucumber.screenplay.events.ScreenPlayEvent;

import static cucumber.screenplay.events.ScreenPlayEvent.Type.*;

public class ScreenplayLifecycleSync {
    private GlobalScope globalScope;

    public ScreenplayLifecycleSync() {

    }

    @StepListener(eventTypes = STEP_STARTED)
    public void startStep(ScreenPlayEvent event) {
        getGlobalScope().getInnerMostActive(StepScope.class).startChildStep(event.getInfo().getName());
    }

    private GlobalScope getGlobalScope() {
        if(globalScope == null){
            globalScope= (GlobalScope) OnStage.performance();
        }
        return globalScope;
    }

    @StepListener(eventTypes = {STEP_PENDING, STEP_SKIPPED, STEP_ASSERTION_FAILED, STEP_SUCCESSFUL, STEP_FAILED})
    public void finishStep(ScreenPlayEvent event) {
        StepScope containingScope = (StepScope) getGlobalScope().getInnerMostActive(StepScope.class).getContainingScope();
        containingScope.completeChildStep(event.getInfo().getName());
    }

}
