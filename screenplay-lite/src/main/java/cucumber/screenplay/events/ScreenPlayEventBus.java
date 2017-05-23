package cucumber.screenplay.events;

import cucumber.screenplay.annotations.StepListener;
import cucumber.screenplay.internal.InstanceGetter;

import java.lang.reflect.Method;
import java.util.*;

public class ScreenPlayEventBus {
    protected InstanceGetter instanceGetter;
    private Map<ScreenPlayEvent.Type, List<ScreenPlayEventCallback>> callbacks = new HashMap<>();

    {
        for (ScreenPlayEvent.Type type : ScreenPlayEvent.Type.values()) {
            callbacks.put(type, new ArrayList<ScreenPlayEventCallback>());
        }
    }

    public ScreenPlayEventBus(InstanceGetter instanceGetter) {
        this.instanceGetter = instanceGetter;
    }

    public void scanClasses(Set<Class<?>> classes) {
        for (Class<?> aClass : classes) {
            for (Method method : aClass.getMethods()) {
                if (method.isAnnotationPresent(StepListener.class)) {
                    for (ScreenPlayEvent.Type type : method.getAnnotation(StepListener.class).eventTypes()) {
                        callbacks.get(type).add(new ScreenPlayEventCallback(instanceGetter.getInstance(aClass), method));
                    }
                }
            }
        }
    }

    public void broadcast(ScreenPlayEvent event) {
        for (ScreenPlayEventCallback callback : callbacks.get(event.getType())) {
            callback.invoke(event, event.getType());
        }
    }
}
