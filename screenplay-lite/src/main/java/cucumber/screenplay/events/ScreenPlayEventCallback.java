package cucumber.screenplay.events;

import cucumber.screenplay.util.NameConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.regex.Pattern;

public class ScreenPlayEventCallback {
    private Method method;
    private String namePattern;
    private Object target;

    public ScreenPlayEventCallback(Object target, Method method,String namePattern) {
        this.target = target;
        this.method = method;
        this.namePattern = namePattern;
    }

    public void invoke(EventObject eventObject, Enum<?> qualifier) {
        try {
            if (method.getParameterTypes().length == 0) {
                method.invoke(target);
            } else if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isInstance(eventObject)) {
                method.invoke(target, eventObject);
            } else if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isInstance(eventObject.getSource())) {
                method.invoke(target, eventObject.getSource());
            } else if (method.getParameterTypes().length == 2 && method.getParameterTypes()[0].isInstance(eventObject.getSource()) && method.getParameterTypes()[1].isInstance(qualifier)) {
                method.invoke(target, eventObject.getSource(), qualifier);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            } else {
                throw new RuntimeException(e.getTargetException());
            }
        }
    }
    protected boolean namesMatch(String nameToMatch) {
        return nameToMatch.equals(namePattern) || Pattern.matches(namePattern, nameToMatch) || NameConverter.filesystemSafe(namePattern).equals(NameConverter.filesystemSafe(nameToMatch))
                || Pattern.matches(namePattern, NameConverter.filesystemSafe(nameToMatch));
    }
}
