package cucumber.screenplay.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventObject;

public class ScreenPlayEventCallback {
    private Method method;
    private Object target;

    public ScreenPlayEventCallback(Object target, Method method) {
        this.target = target;
        this.method = method;
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
}
