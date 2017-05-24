package cucumber.scoping;

import cucumber.screenplay.util.NameConverter;

import java.util.ArrayList;
import java.util.List;

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
        this.id = NameConverter.filesystemSafe(name);
    }

    protected List<VerificationScope> getNestedScopes() {
        return nestedScopes;
    }

    public int getLevel() {
        return getContainingScope().getLevel() + 1;
    }

    protected void completeWithoutEvents() {
        this.active = false;
    }

    protected void completeChildren() {
        for (VerificationScope nestedScope : nestedScopes) {
            nestedScope.complete();
        }
    }

    public void complete() {
        completeWithoutEvents();
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

    public void start() {
        startWithoutEvents();
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
