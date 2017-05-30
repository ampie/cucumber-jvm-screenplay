package com.sbg.bdd.screenplay.scoped.listeners;

import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.annotations.StepListener;
import com.sbg.bdd.screenplay.core.events.StepEvent;
import com.sbg.bdd.screenplay.scoped.GlobalScope;
import com.sbg.bdd.screenplay.scoped.ScenarioScope;
import com.sbg.bdd.screenplay.scoped.StepScope;

import static com.sbg.bdd.screenplay.core.annotations.StepEventType.*;

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
