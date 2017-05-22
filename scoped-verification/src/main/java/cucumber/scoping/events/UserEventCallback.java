package cucumber.scoping.events;


import cucumber.scoping.UserInScope;
import cucumber.scoping.annotations.UserInvolvement;
import cucumber.scoping.annotations.SubscribeToUser;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static cucumber.scoping.IdGenerator.fromName;
import static cucumber.scoping.events.ScopeEventBus.mostSpecific;

;

public class UserEventCallback {
    private final Class<?> type;
    private Method method;
    private UserInvolvement timing;
    private String scopeNamePattern;
    private int level;
    private Object target;

    public UserEventCallback(Object target, Method method, SubscribeToUser b) {
        this.target = target;
        this.method = method;
        timing = b.involvement();
        scopeNamePattern = b.namePattern();
        this.level = b.level();
        type= mostSpecific(method,b.scopeType());
    }

    public void execute(UserInScope scope, UserInvolvement phase) {
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

    public boolean isMatch(UserEvent event) {
       return type.isInstance(event.getUserInScope()) && this.timing == event.getInvolvement() && levelsMatch(event.getUserInScope().getScope().getLevel()) &&
                namesMatch(event.getUserInScope().getName());
    }

    private boolean levelsMatch(int levelToMatch) {
        return level == -1 || levelToMatch == level;
    }

    private boolean namesMatch(String nameToMatch) {
        return nameToMatch.equals(scopeNamePattern) || Pattern.matches(scopeNamePattern, nameToMatch) || fromName(scopeNamePattern).equals(fromName(nameToMatch))
                || Pattern.matches(scopeNamePattern, fromName(nameToMatch));
    }

}
