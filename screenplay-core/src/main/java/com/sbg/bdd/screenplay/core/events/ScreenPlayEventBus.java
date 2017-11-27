package com.sbg.bdd.screenplay.core.events;

import com.sbg.bdd.screenplay.core.annotations.*;
import com.sbg.bdd.screenplay.core.internal.InstanceGetter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ScreenPlayEventBus {
    private static List<Pair<Enum<?>, ScreenPlayEventCallback>> registeredCallbacks = Collections.synchronizedList(new ArrayList<Pair<Enum<?>, ScreenPlayEventCallback>>());

    public static void registerCallback(Enum<?> type,ScreenPlayEventCallback callback) {
        registeredCallbacks.add(new ImmutablePair<Enum<?>, ScreenPlayEventCallback>(type, callback));
    }

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
    private Map<StepEventType, SortedSet<StepEventCallback>> stepListeners = new HashMap<>();

    {
        for (StepEventType stepEventType : StepEventType.values()) {
            if (stepEventType == StepEventType.STARTED) {
                stepListeners.put(stepEventType, new TreeSet<StepEventCallback>(new CallbackNestingSequence()));
            } else {
                stepListeners.put(stepEventType, new TreeSet<StepEventCallback>(Collections.reverseOrder(new CallbackNestingSequence())));
            }
        }
    }

    private Map<SceneEventType, SortedSet<SceneEventCallback>> sceneListeners = new HashMap<>();

    {
        for (SceneEventType phase : SceneEventType.values()) {
            sceneListeners.put(phase, new TreeSet<SceneEventCallback>(new CallbackNestingSequence()));
        }
    }

    private Map<ActorInvolvement, SortedSet<OnStageActorEventCallback>> onStageActorEventListeners = new HashMap<>();

    {
        for (ActorInvolvement actorInvolvement : ActorInvolvement.values()) {
            onStageActorEventListeners.put(actorInvolvement, new TreeSet<OnStageActorEventCallback>(new CallbackNestingSequence()));
        }
    }

    private Map<ActorEventType, SortedSet<ActorEventCallback>> actorEventListeners = new HashMap<>();

    {
        for (ActorEventType actorInvolvement : ActorEventType.values()) {
            actorEventListeners.put(actorInvolvement, new TreeSet<ActorEventCallback>(new CallbackNestingSequence()));
        }
    }
    public ScreenPlayEventBus(InstanceGetter instanceGetter) {
        this.instanceGetter = instanceGetter;
    }

    public void scanClasses(Set<Class<?>> classes) {
        for (Class<?> aClass : classes) {
            if(!Modifier.isAbstract(aClass.getModifiers())) {
                scanMethods(aClass);
            }
        }
    }

    private void scanMethods(Class<?> aClass) {
        for (Method method : aClass.getMethods()) {
            if (method.isAnnotationPresent(StepListener.class)) {
                StepListener stepListener = method.getAnnotation(StepListener.class);
                for (StepEventType stepEventType : stepListener.eventTypes()) {
                    StepEventCallback callback = new StepEventCallback(instanceGetter.getInstance(aClass), method, stepListener);
                    register(stepEventType, callback);
                }
            }
            if (method.isAnnotationPresent(SceneListener.class)) {
                Object target = instanceGetter.getInstance(aClass);
                for (SceneEventType sceneEventType : method.getAnnotation(SceneListener.class).scopePhases()) {
                    SceneEventCallback callback = new SceneEventCallback(target, method, method.getAnnotation(SceneListener.class));
                    register(sceneEventType, callback);
                }
            }
            if (method.isAnnotationPresent(ActorInvolvementListener.class)) {
                Object target = instanceGetter.getInstance(aClass);
                for (ActorInvolvement involvement : method.getAnnotation(ActorInvolvementListener.class).involvement()) {
                    OnStageActorEventCallback callback = new OnStageActorEventCallback(target, method, method.getAnnotation(ActorInvolvementListener.class));
                    register(involvement, callback);
                }
            }
            if (method.isAnnotationPresent(ActorListener.class)) {
                Object target = instanceGetter.getInstance(aClass);
                for (ActorEventType involvement : method.getAnnotation(ActorListener.class).eventType()) {
                    ActorEventCallback callback = new ActorEventCallback(target, method, method.getAnnotation(ActorListener.class));
                    register(involvement, callback);
                }
            }
        }
    }

    public void register(ActorEventType involvement, ActorEventCallback callback) {
        this.actorEventListeners.get(involvement).add(callback);
    }
    public void register(ActorInvolvement involvement, OnStageActorEventCallback callback) {
        this.onStageActorEventListeners.get(involvement).add(callback);
    }

    public void register(SceneEventType sceneEventType, SceneEventCallback callback) {
        this.sceneListeners.get(sceneEventType).add(callback);
    }

    public void register(StepEventType stepEventType, StepEventCallback callback) {
        stepListeners.get(stepEventType).add(callback);
    }

    public void broadcast(StepEvent event) {
        ensureRegisteredCallbacksIncluded();
        for (StepEventCallback callback : stepListeners.get(event.getType())) {
            callback.invoke(event, event.getType());
        }
    }

    public void broadcast(SceneEvent event) {
        ensureRegisteredCallbacksIncluded();
        for (SceneEventCallback callback : sceneListeners.get(event.getSceneEventType())) {
            if (callback.isMatch(event)) {
                callback.invoke(event, event.getSceneEventType());
            }
        }
    }

    public void broadcast(OnStageActorEvent event) {
        ensureRegisteredCallbacksIncluded();
        for (OnStageActorEventCallback callback : onStageActorEventListeners.get(event.getInvolvement())) {
            if (callback.isMatch(event)) {
                callback.invoke(event, event.getInvolvement());
            }
        }
    }

    public void broadcast(ActorEvent actorEvent) {

    }
    private void ensureRegisteredCallbacksIncluded() {
        Iterator<Pair<Enum<?>, ScreenPlayEventCallback>> iterator = registeredCallbacks.iterator();
        while (iterator.hasNext()) {
            Pair<Enum<?>, ScreenPlayEventCallback> callback = iterator.next();
            if (callback.getRight() instanceof StepEventCallback) {
                register((StepEventType) callback.getLeft(), (StepEventCallback) callback.getRight());
            } else if (callback.getRight() instanceof SceneEventCallback) {
                register((SceneEventType) callback.getLeft(), (SceneEventCallback) callback.getRight());
            } else if (callback.getRight() instanceof OnStageActorEventCallback) {
                register((ActorInvolvement) callback.getLeft(), (OnStageActorEventCallback) callback.getRight());
            }
            iterator.remove();
        }

    }

}
