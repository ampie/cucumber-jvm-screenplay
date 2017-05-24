package cucumber.screenplay.events;

import cucumber.screenplay.annotations.*;
import cucumber.screenplay.internal.InstanceGetter;

import java.lang.reflect.Method;
import java.util.*;

public class ScreenPlayEventBus {
    public static Class<?> mostSpecific(Method method, Class<?> aClass) {
        if (method.getParameterTypes().length == 0) {
            return aClass;
        } else {
            if (aClass.isAssignableFrom(method.getParameterTypes()[0])) {
                return method.getParameterTypes()[0];
            } else {
                return aClass;
            }
        }
    }
    protected InstanceGetter instanceGetter;
    private Map<StepEventType, List<ScreenPlayEventCallback>> stepListeners = new HashMap<>();

    {
        for (StepEventType stepEventType : StepEventType.values()) {
            stepListeners.put(stepEventType, new ArrayList<ScreenPlayEventCallback>());
        }
    }

    private Map<SceneEventType, List<SceneEventCallback>> sceneListeners = new HashMap<>();

    {
        for (SceneEventType phase : SceneEventType.values()) {
            sceneListeners.put(phase, new ArrayList<SceneEventCallback>());
        }
    }

    private Map<ActorInvolvement, List<ActorEventCallback>> actorListeners = new HashMap<>();

    {
        for (ActorInvolvement actorInvolvement : ActorInvolvement.values()) {
            actorListeners.put(actorInvolvement, new ArrayList<ActorEventCallback>());
        }
    }

    public ScreenPlayEventBus(InstanceGetter instanceGetter) {
        this.instanceGetter = instanceGetter;
    }

    public void scanClasses(Set<Class<?>> classes) {
        for (Class<?> aClass : classes) {
            scanMethods(aClass);
        }
    }

    private void scanMethods(Class<?> aClass) {
        for (Method method : aClass.getMethods()) {
            if (method.isAnnotationPresent(StepListener.class)) {
                StepListener stepListener = method.getAnnotation(StepListener.class);
                for (StepEventType stepEventType : stepListener.eventTypes()) {
                    stepListeners.get(stepEventType).add(new ScreenPlayEventCallback(instanceGetter.getInstance(aClass), method,stepListener.namePattern()));
                }
            }
            if (method.isAnnotationPresent(SceneListener.class)) {
                Object target = instanceGetter.getInstance(aClass);
                for (SceneEventType sceneEventType : method.getAnnotation(SceneListener.class).scopePhases()) {
                    List<SceneEventCallback> scopeSubscribers = this.sceneListeners.get(sceneEventType);
                    scopeSubscribers.add(new SceneEventCallback(target, method, method.getAnnotation(SceneListener.class), sceneEventType));
                }
            }
            if (method.isAnnotationPresent(ActorListener.class)) {
                Object target = instanceGetter.getInstance(aClass);
                for (ActorInvolvement involvement : method.getAnnotation(ActorListener.class).involvement()) {
                    List<ActorEventCallback> userSubscribers = this.actorListeners.get(involvement);
                    userSubscribers.add(new ActorEventCallback(target, method, method.getAnnotation(ActorListener.class)));
                }
            }
        }
    }

    public void broadcast(StepEvent event) {
        for (ScreenPlayEventCallback callback : stepListeners.get(event.getType())) {
            callback.invoke(event, event.getType());
        }
    }

    public void broadcast(SceneEvent event) {
        for (SceneEventCallback callback : sceneListeners.get(event.getSceneEventType())) {
            if (callback.isMatch(event)) {
                callback.invoke(event, event.getSceneEventType());
            }
        }
    }

    public void broadcast(ActorEvent event) {
        for (ActorEventCallback callback : actorListeners.get(event.getInvolvement())) {
            if (callback.isMatch(event)) {
                callback.invoke(event, event.getInvolvement());
            }
        }
    }
}
