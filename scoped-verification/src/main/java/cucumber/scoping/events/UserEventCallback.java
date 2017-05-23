package cucumber.scoping.events;


import cucumber.scoping.annotations.SubscribeToUser;
import cucumber.scoping.annotations.UserInvolvement;
import cucumber.screenplay.events.ScreenPlayEventCallback;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static cucumber.scoping.IdGenerator.fromName;
import static cucumber.scoping.events.ScopeEventBus.mostSpecific;

;

public class UserEventCallback extends ScreenPlayEventCallback {
    private final Class<?> type;
    private UserInvolvement involvement;
    private String scopeNamePattern;
    private int level;

    public UserEventCallback(Object target, Method method, SubscribeToUser b,UserInvolvement involvement) {
        super(target, method);
        this.involvement = involvement;
        scopeNamePattern = b.namePattern();
        this.level = b.level();
        type = mostSpecific(method, b.scopeType());
    }

    public boolean isMatch(UserEvent event) {
        return type.isInstance(event.getUserInScope()) && this.involvement == event.getInvolvement() && levelsMatch(event.getUserInScope().getScope().getLevel()) &&
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
