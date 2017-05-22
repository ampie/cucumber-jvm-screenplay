package cucumber.scoping.glue;

import cucumber.api.java.en.Given;
import cucumber.scoping.ActorInScope;
import cucumber.scoping.FunctionalScope;
import cucumber.scoping.ScenarioScope;
import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.annotations.SubscribeToScope;
import cucumber.scoping.annotations.SubscribeToUser;
import cucumber.scoping.annotations.UserInvolvement;
import cucumber.scoping.screenplay.ScopedActor;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.DownstreamStub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cucumber.screenplay.ScreenplayPhrases.actorNamed;
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by ampie on 2017/05/21.
 */
public class StepDefs {

    public static Map<String,List<Object>> SCOPE_CALLBACKS = new HashMap<>();
    public static Map<String,List<Object>> USER_CALLBACKS = new HashMap<>();
    public static Map<String,Integer> VARIABLE_BEFORE_START = new HashMap<>();
    public static Map<String,Integer> VARIABLE_AFTER_START = new HashMap<>();
    public static Map<String,Integer> VARIABLE_AFTER_COMPLETE = new HashMap<>();
    @Given("asd")
    public void stuff(){

    }
    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_START)
    public void beforeStart(FunctionalScope scope,ScopePhase phase){
        VARIABLE_BEFORE_START.put(scope.getScopePath(),scope.<Integer>recall("scopeLevel"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SubscribeToScope(scopePhase = ScopePhase.AFTER_START)
    public void afterStart(FunctionalScope scope,ScopePhase phase){
        scope.remember("scopeLevel",scope.getLevel());
        VARIABLE_AFTER_START.put(scope.getScopePath(),scope.<Integer>recall("scopeLevel"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
        scope.enter(actorNamed("John"));
    }

    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_COMPLETE)
    public void beforeComplete(FunctionalScope scope,ScopePhase phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }
    @SubscribeToScope(scopePhase = ScopePhase.AFTER_COMPLETE)
    public void afterComplete(FunctionalScope scope,ScopePhase phase){
        VARIABLE_AFTER_COMPLETE.put(scope.getScopePath(),scope.<Integer>recall("scopeLevel"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }


    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_START)
    public void beforeStart(ScenarioScope scope,ScopePhase phase){
        VARIABLE_BEFORE_START.put(scope.getScopePath(),scope.<Integer>recall("scopeLevel"));
        scope.enter(actorNamed("John"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SubscribeToScope(scopePhase = ScopePhase.AFTER_START)
    public void afterStart(ScenarioScope scope,ScopePhase phase){
        scope.remember("scopeLevel",scope.getLevel());
        VARIABLE_AFTER_START.put(scope.getScopePath(),scope.<Integer>recall("scopeLevel"));
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_COMPLETE)
    public void beforeComplete(ScenarioScope scope,ScopePhase phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }
    @SubscribeToScope(scopePhase = ScopePhase.AFTER_COMPLETE)
    public void afterComplete(ScenarioScope scope,ScopePhase phase){
        VARIABLE_AFTER_COMPLETE.put(scope.getScopePath(),scope.<Integer>recall("scopeLevel"));
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
    public void beforeExit(ActorInScope scope){
        registerCallbackOccurrence(scope.getScopePath(), UserInvolvement.BEFORE_EXIT);
    }
    @SubscribeToUser(involvement = UserInvolvement.AFTER_EXIT)
    public void afterExit(ActorInScope scope,UserInvolvement phase){
        registerCallbackOccurrence(scope.getScopePath(), phase);
    }

    private void registerCallbackOccurrence(String scopePath,  ScopePhase phase) {
        registerCallbackOccurrence(scopePath, phase, SCOPE_CALLBACKS);
    }
    private void registerCallbackOccurrence(String scopePath,  UserInvolvement phase) {
        registerCallbackOccurrence(scopePath, phase, USER_CALLBACKS);
    }

    private void registerCallbackOccurrence(String scopePath, Enum<?> phase, Map<String, List<Object>> map) {
        List<Object> eventPhases = map.get(scopePath);
        if(eventPhases==null){
            eventPhases=new ArrayList<>();
            map.put(scopePath,eventPhases);
        }
        eventPhases.add(phase);
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
