package com.sbg.bdd.cucumber.screenplay.scoped;

import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.DownstreamStub;
import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement;
import com.sbg.bdd.screenplay.core.annotations.ActorInvolvementListener;
import com.sbg.bdd.screenplay.core.annotations.SceneEventType;
import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.scoped.ActorInScope;
import com.sbg.bdd.screenplay.scoped.FunctionalScope;
import com.sbg.bdd.screenplay.scoped.ScenarioScope;
import cucumber.api.java.en.Given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed;
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom;


public class StepDefs {

    public static Map<String, List<Object>> SCOPE_CALLBACKS = new HashMap<>();
    public static Map<String, List<Object>> USER_CALLBACKS = new HashMap<>();
    public static Map<String, Integer> VARIABLE_BEFORE_START = new HashMap<>();
    public static Map<String, Integer> VARIABLE_AFTER_START = new HashMap<>();
    public static Map<String, Integer> VARIABLE_AFTER_COMPLETE = new HashMap<>();

    @Given("asd")
    public void stuff() {

    }

    @SceneListener(scopePhases = SceneEventType.BEFORE_START)
    public void beforeStart(FunctionalScope scope, SceneEventType phase) {
        VARIABLE_BEFORE_START.put(scope.getScopePath(), scope.getEverybodyScope().<Integer>recall("scopeLevel"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SceneListener(scopePhases = SceneEventType.AFTER_START)
    public void afterStart(FunctionalScope scope, SceneEventType phase) {
        scope.getEverybodyScope().remember("scopeLevel", scope.getLevel());
        VARIABLE_AFTER_START.put(scope.getScopePath(), scope.getEverybodyScope().<Integer>recall("scopeLevel"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
        scope.shineSpotlightOn(actorNamed("John"));
    }

    @SceneListener(scopePhases = SceneEventType.BEFORE_COMPLETE)
    public void beforeComplete(FunctionalScope scope, SceneEventType phase) {
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SceneListener(scopePhases = SceneEventType.AFTER_COMPLETE)
    public void afterComplete(FunctionalScope scope, SceneEventType phase) {
        VARIABLE_AFTER_COMPLETE.put(scope.getScopePath(), scope.getEverybodyScope().<Integer>recall("scopeLevel"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }


    @SceneListener(scopePhases = SceneEventType.BEFORE_START)
    public void beforeStart(ScenarioScope scope, SceneEventType phase) {
        VARIABLE_BEFORE_START.put(scope.getScopePath(), scope.getEverybodyScope().<Integer>recall("scopeLevel"));
        scope.shineSpotlightOn(actorNamed("John"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SceneListener(scopePhases = SceneEventType.AFTER_START)
    public void afterStart(ScenarioScope scope, SceneEventType phase) {
        scope.getEverybodyScope().remember("scopeLevel", scope.getLevel());
        VARIABLE_AFTER_START.put(scope.getScopePath(), scope.getEverybodyScope().<Integer>recall("scopeLevel"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SceneListener(scopePhases = SceneEventType.BEFORE_COMPLETE)
    public void beforeComplete(ScenarioScope scope, SceneEventType phase) {
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SceneListener(scopePhases = SceneEventType.AFTER_COMPLETE)
    public void afterComplete(ScenarioScope scope, SceneEventType phase) {
        VARIABLE_AFTER_COMPLETE.put(scope.getScopePath(), scope.getEverybodyScope().<Integer>recall("scopeLevel"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @ActorInvolvementListener(involvement = ActorInvolvement.BEFORE_ENTER_STAGE)
    public void beforeEnter(ActorInScope scope, ActorInvolvement phase) {
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @ActorInvolvementListener(involvement = ActorInvolvement.AFTER_ENTER_STAGE)
    public void afterEnter(ActorInScope scope, ActorInvolvement phase) {
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @ActorInvolvementListener(involvement = ActorInvolvement.BEFORE_EXIT_STAGE)
    public void beforeExit(ActorInScope scope) {
        registerCallbackOccurrence(scope.getScopePath(), ActorInvolvement.BEFORE_EXIT_STAGE);
    }

    @ActorInvolvementListener(involvement = ActorInvolvement.AFTER_EXIT_STAGE)
    public void afterExit(ActorInScope scope, ActorInvolvement phase) {
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    private void registerCallbackOccurrence(String scopePath, SceneEventType phase) {
        registerCallbackOccurrence(scopePath, phase, SCOPE_CALLBACKS);
    }

    private void registerCallbackOccurrence(String scopePath, ActorInvolvement phase) {
        registerCallbackOccurrence(scopePath, phase, USER_CALLBACKS);
    }

    private void registerCallbackOccurrence(String scopePath, Enum<?> phase, Map<String, List<Object>> map) {
        List<Object> eventPhases = map.get(scopePath);
        if (eventPhases == null) {
            eventPhases = new ArrayList<>();
            map.put(scopePath, eventPhases);
        }
        eventPhases.add(phase);
    }

    @Given("^a step is performed$")
    public void aStepIsPerformed() throws Throwable {

        Actor john = actorNamed("John");
        forRequestsFrom(john).allow(new DownstreamStub() {
            @Override
            public void performOnStage(ActorOnStage actorOnStage) {

            }
        });
    }
}
