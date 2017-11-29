package com.sbg.bdd.screenplay.scoped;

/**
 * Represents any functional scope that could contain Scenarios,
 * e.g. Scenario Outlines, Features, Stories, Directories of features
 */
public class FunctionalScope extends UserTrackingScope {


    public FunctionalScope(UserTrackingScope containingScope, String name) {
        super(containingScope, name);
    }

    public ScenarioScope startScenario(String name){
        return setupChild(new ScenarioScope(this,name));
    }
    public FunctionalScope startNestedScope(String name){
        return setupChild(new FunctionalScope(this,name));
    }
}
