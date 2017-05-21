package cucumber.scoping.events;


import cucumber.scoping.VerificationScope;
import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.annotations.SubscribeToScope;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

;import static cucumber.scoping.IdGenerator.fromName;

public class ScopedCallback {
    private Method method;
    private ScopePhase timing;
    private String scopeNamePattern;
    private int level;
    private Object target;

    public ScopedCallback(Object target, Method method, SubscribeToScope b) {
        this.target = target;
        this.method = method;
        timing = b.scopePhase();
        scopeNamePattern = b.namePattern();
        this.level = b.level();
    }


    public void execute(VerificationScope scope, ScopePhase phase) {
        try {
            invoke(scope, phase);
        } catch (Exception e) {
            //Yeah think about this
            throw new RuntimeException(e);
        }

    }

    private void invoke(VerificationScope scope, ScopePhase phase) throws Exception {
        if (method.getParameterTypes().length == 0) {
            method.invoke(getTarget());
        } else if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isInstance(scope)) {
            method.invoke(getTarget(), scope);
        } else if (method.getParameterTypes().length == 2 && method.getParameterTypes()[0].isInstance(scope) && method.getParameterTypes()[1].isInstance(phase)) {
            method.invoke(getTarget(), scope, phase);
        }
    }

    private Object getTarget() {
        return target;
    }

    public boolean isMatch(ScopeEvent event) {
        return this.timing == event.getScopePhase() && levelsMatch(event.getScope().getLevel()) &&
                namesMatch(event.getScope().getName());
    }

    private boolean levelsMatch(int levelToMatch) {
        return levelToMatch == -1 || levelToMatch == level;
    }

    private boolean namesMatch(String nameToMatch) {
        return nameToMatch.equals(scopeNamePattern) || Pattern.matches(scopeNamePattern, nameToMatch) || fromName(scopeNamePattern).equals(fromName(nameToMatch))
                || Pattern.matches(scopeNamePattern, fromName(nameToMatch));
    }

}
