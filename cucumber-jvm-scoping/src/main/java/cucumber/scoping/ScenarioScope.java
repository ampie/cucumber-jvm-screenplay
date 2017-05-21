package cucumber.scoping;


public class ScenarioScope extends UserTrackingScope {
    public ScenarioScope(FunctionalScope functionalScope, String name) {
        super(functionalScope, name);
    }

    public StepScope startStep(String name){
        return setupChild(new StepScope(this,name));
    }
    public void evaluateVerificationRules(){
        for (UserInScope userInScope : getUsersInScope().values()) {
            userInScope.evaluateVerificationRules();
        }
    }


    public void completeStep(String name) {
        super.completeNestedScope(name);
    }

}
