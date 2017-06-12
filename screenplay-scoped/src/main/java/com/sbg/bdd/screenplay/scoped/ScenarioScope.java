package com.sbg.bdd.screenplay.scoped;


public class ScenarioScope extends UserTrackingScope {
    public ScenarioScope(FunctionalScope functionalScope, String name) {
        super(functionalScope, name);
    }

    @Override
    protected void completeWithoutEvents() {
        evaluateVerificationRules();
        super.completeWithoutEvents();
    }

    public void evaluateVerificationRules() {
        for (UserInScope userInScope : getUsersInScope().values()) {
            userInScope.evaluateExpectations();
        }
    }


}
