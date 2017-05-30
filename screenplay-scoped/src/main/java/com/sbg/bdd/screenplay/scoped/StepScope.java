package com.sbg.bdd.screenplay.scoped;


public class StepScope extends VerificationScope {
    public StepScope(VerificationScope containingScope, String name) {
        super(containingScope, name);
    }

    public StepScope startChildStep(String name) {
        return setupChild(new StepScope(this, name));
    }
    public String getStepPath(){
        if(getContainingScope() instanceof StepScope){
            return ((StepScope) getContainingScope()).getStepPath() + "/" + getId();
        }else{
            return getId();
        }
    }

    public void completeChildStep(String name) {
        super.completeNestedScope(name);
    }
}
