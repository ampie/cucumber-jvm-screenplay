package cucumber.scoping.events;


import cucumber.scoping.UserInScope;
import cucumber.scoping.VerificationScope;
import cucumber.scoping.annotations.UserInvolvement;
import cucumber.scoping.annotations.SubscribeToUser;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static cucumber.scoping.IdGenerator.fromName;

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
        type=mostSpecific(method,b.scopeType());
    }

    private Class<?> mostSpecific(Method method, Class<?> aClass) {
        if(method.getParameterTypes().length==0){
            return aClass;
        }else{
            if(aClass.isAssignableFrom(method.getParameterTypes()[0])){
                return method.getParameterTypes()[0];
            }else{
                return aClass;
            }
        }
    }

    public void execute(UserInScope scope, UserInvolvement phase) {
        try {
            invoke(scope, phase);
        } catch (Exception e) {
            //Yeah think about this
            throw new RuntimeException(e);
        }

    }

    private void invoke(UserInScope scope, UserInvolvement phase) throws Exception {
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
