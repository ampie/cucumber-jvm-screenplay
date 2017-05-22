package cucumber.scoping;

import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.events.ScopeEvent;

import java.util.ArrayList;
import java.util.List;

import static cucumber.scoping.IdGenerator.fromName;

public class VerificationScope {
    private String id;
    private String name;
    private boolean active = false;

    private VerificationScope containingScope;
    protected boolean hasActiveChild = false;
    private List<VerificationScope> nestedScopes = new ArrayList<>();


    public VerificationScope(VerificationScope containingScope, String name) {
        this.containingScope = containingScope;
        this.name = name;
        this.id = fromName(name);
    }

    public int getLevel() {
        return getContainingScope().getLevel() + 1;
    }

    public final void complete() {
        if (isActive()) {
            //We only complete once
            getGlobalScope().broadcast(new ScopeEvent(this, ScopePhase.BEFORE_COMPLETE));
            completeWithoutEvents();
            getGlobalScope().broadcast(new ScopeEvent(this, ScopePhase.AFTER_COMPLETE));
        }
    }

    protected void completeWithoutEvents() {
        for (VerificationScope nestedScope : nestedScopes) {
            nestedScope.complete();
        }
        this.active = false;
    }

    public <T> T getInnerMostActive(Class<T> ofType) {
        T result = null;
        if (!isActive()) {
            return null;
        } else if (hasActiveChild) {
            result = getActiveNestedScope().getInnerMostActive(ofType);
        }
        if (result == null && ofType.isInstance(this)) {
            return (T) this;
        } else {
            return result;
        }
    }

    public <T> T getNearestContaining(Class<T> type) {
        if (type.isInstance(getContainingScope())) {
            return (T) getContainingScope();
        } else {
            return getContainingScope().getNearestContaining(type);
        }
    }

    public VerificationScope getActiveNestedScope() {
        return hasActiveChild ? nestedScopes.get(nestedScopes.size() - 1) : null;
    }

    protected <T extends VerificationScope> T setupChild(T child) {
        nestedScopes.add(child);
        hasActiveChild = true;
        child.start();
        return child;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getScopePath() {
        return this.getContainingScope().getScopePath() + "/" + getId();
    }

    public boolean isActive() {
        return this.active;
    }

    public void start() {
        if (!isActive()) {
            //WE only start once
            getGlobalScope().broadcast(new ScopeEvent(this, ScopePhase.BEFORE_START));
            startWithoutEvents();
            getGlobalScope().broadcast(new ScopeEvent(this, ScopePhase.AFTER_START));
        }
    }

    protected void startWithoutEvents() {
        this.active = true;
    }


    public GlobalScope getGlobalScope() {
        return getContainingScope().getGlobalScope();
    }

    public VerificationScope getContainingScope() {
        return containingScope;
    }

    public VerificationScope completeNestedScope(String childName) {
        if (getActiveNestedScope() == null) {
            System.out.printf("");
        }
        if (childName.equals(getActiveNestedScope().getName())) {
            VerificationScope scope = getActiveNestedScope();
            scope.complete();
            hasActiveChild = false;
            return scope;
        } else {
            //Just a check to ensure the source of the lifecycle management is not out of sync
            throw new IllegalArgumentException(childName + " is not active, " + getActiveNestedScope().getName() + " is");
        }
    }

}
