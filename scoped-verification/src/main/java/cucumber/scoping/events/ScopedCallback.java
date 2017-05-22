package cucumber.scoping.events;


import cucumber.scoping.VerificationScope;
import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.annotations.SubscribeToScope;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

;import static cucumber.scoping.IdGenerator.fromName;
import static cucumber.scoping.events.ScopeEventBus.mostSpecific;

public class ScopedCallback {
    private Method method;
    private ScopePhase timing;
    private String scopeNamePattern;
    private int level;
    private Object target;
    private Class<?> type;

    public ScopedCallback(Object target, Method method, SubscribeToScope b) {
        this.target = target;
        this.method = method;
        timing = b.scopePhase();
        scopeNamePattern = b.namePattern();
        this.level = b.level();
        type=mostSpecific(method,b.scopeType());
    }




    public void execute(VerificationScope scope, ScopePhase phase) {
        try {
            ScopeEventBus.invoke(getTarget(), scope, phase, this.method);

        } catch (Exception e) {
            //Yeah think about this
            throw new RuntimeException(e);
        }

    }


    private Object getTarget() {
        return target;
    }

    public boolean isMatch(ScopeEvent event) {
        return type.isInstance(event.getScope()) &&  this.timing == event.getScopePhase() && levelsMatch(event.getScope().getLevel()) &&
                namesMatch(event.getScope().getName());
    }

    private boolean levelsMatch(int levelToMatch) {
        return level == -1 || levelToMatch == level;
    }

    private boolean namesMatch(String nameToMatch) {
        return nameToMatch.equals(scopeNamePattern) || Pattern.matches(scopeNamePattern, nameToMatch) || fromName(scopeNamePattern).equals(fromName(nameToMatch))
                || Pattern.matches(scopeNamePattern, fromName(nameToMatch));
    }

}
