package cucumber.scoping.events;

import cucumber.scoping.VerificationScope;
import cucumber.scoping.annotations.ScopePhase;

import java.util.EventObject;

public class ScopeEvent  extends EventObject {
    private final ScopePhase scopePhase;

    public ScopeEvent(VerificationScope source, ScopePhase scopePhase) {
        super(source);
        this.scopePhase=scopePhase;
    }

    public ScopePhase getScopePhase() {
        return scopePhase;
    }
    public VerificationScope getScope(){
        return (VerificationScope) getSource();
    }
}
