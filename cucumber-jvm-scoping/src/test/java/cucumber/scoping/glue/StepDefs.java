package cucumber.scoping.glue;

import cucumber.api.java.en.Given;
import cucumber.scoping.*;
import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.annotations.SubscribeToScope;
import cucumber.scoping.annotations.SubscribeToUser;
import cucumber.scoping.annotations.UserInvolvement;
import cucumber.scoping.screenplay.ScopedActor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.DownstreamStub;

import java.util.*;

import static cucumber.screenplay.ScreenplayPhrases.actorNamed;
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom;

/**
 * Created by ampie on 2017/05/21.
 */
public class StepDefs {
    public static Map<String,List<Object>> SCOPE_CALLBACKS = new HashMap<>();
    @Given("asd")
    public void stuff(){

    }
    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_START)
    public void beforeStart(FunctionalScope scope,ScopePhase phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SubscribeToScope(scopePhase = ScopePhase.AFTER_START)
    public void afterStart(FunctionalScope scope,ScopePhase phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_COMPLETE)
    public void beforeComplete(FunctionalScope scope,ScopePhase phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }
    @SubscribeToScope(scopePhase = ScopePhase.AFTER_COMPLETE)
    public void afterComplete(FunctionalScope scope,ScopePhase phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }


    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_START)
    public void beforeStart(ScenarioScope scope,ScopePhase phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SubscribeToScope(scopePhase = ScopePhase.AFTER_START)
    public void afterStart(ScenarioScope scope,ScopePhase phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_COMPLETE)
    public void beforeComplete(ScenarioScope scope,ScopePhase phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }
    @SubscribeToScope(scopePhase = ScopePhase.AFTER_COMPLETE)
    public void afterComplete(ScenarioScope scope,ScopePhase phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SubscribeToUser(involvement = UserInvolvement.BEFORE_ENTER)
    public void beforeEnter(ActorInScope scope, UserInvolvement phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SubscribeToUser(involvement = UserInvolvement.AFTER_ENTER)
    public void afterEnter(ActorInScope scope,UserInvolvement phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SubscribeToUser(involvement = UserInvolvement.BEFORE_EXIT)
    public void beforeExit(ActorInScope scope,UserInvolvement phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }
    @SubscribeToUser(involvement = UserInvolvement.AFTER_EXIT)
    public void afterExit(ActorInScope scope,UserInvolvement phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    private void registerCallbackOccurrence(String scopePath,  Enum<?> phase) {
        List<Object> scopePhases = SCOPE_CALLBACKS.get(scopePath);
        if(scopePhases==null){
            scopePhases=new ArrayList<>();
            SCOPE_CALLBACKS.put(scopePath,scopePhases);
        }
        scopePhases.add(phase);
    }

    @Given("^a step is performed$")
    public void aStepIsPerformed() throws Throwable {

        ScopedActor john = actorNamed("John");
        forRequestsFrom(john).allow(new DownstreamStub() {
            @Override
            public void performOnStage(ActorOnStage actorOnStage) {

            }
        });
        System.out.println(john.getPersona().getUserName());
    }
}
