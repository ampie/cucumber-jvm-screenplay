package cucumber.scoping.events;

import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.annotations.SubscribeToScope;
import cucumber.scoping.annotations.SubscribeToUser;
import cucumber.scoping.annotations.UserInvolvement;
import cucumber.screenplay.events.ScreenPlayEventBus;
import cucumber.screenplay.internal.InstanceGetter;

import java.lang.reflect.Method;
import java.util.*;

public class ScopeEventBus extends ScreenPlayEventBus {
    
    private Map<ScopePhase, List<ScopedCallback>> scopeSubscribers = new HashMap<>();{
        for (ScopePhase phase : ScopePhase.values()) {
            scopeSubscribers.put(phase,new ArrayList<ScopedCallback>());
        }
    }
    private Map<UserInvolvement, List<UserEventCallback>> userSubscribers = new HashMap<>();
    {
        for (UserInvolvement userInvolvement : UserInvolvement.values()) {
            userSubscribers.put(userInvolvement,new ArrayList<UserEventCallback>());
        }
    }
    public ScopeEventBus(InstanceGetter objectFactory) {
        super(objectFactory);
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

    
    public void scanClasses(Set<Class<?>> classes) {
        super.scanClasses(classes);
        for (Class<?> aClass : classes) {
            scanMethods(aClass);
        }
    }
    
    
    private void scanMethods(Class<?> aClass) {
        for (Method method : aClass.getMethods()) {
            if (method.isAnnotationPresent(SubscribeToScope.class)) {
                Object target = instanceGetter.getInstance(aClass);
                for (ScopePhase scopePhase : method.getAnnotation(SubscribeToScope.class).scopePhases()) {
                    List<ScopedCallback> scopeSubscribers = this.scopeSubscribers.get(scopePhase);
                    scopeSubscribers.add(new ScopedCallback(target, method, method.getAnnotation(SubscribeToScope.class),scopePhase));
                }
            }
            if (method.isAnnotationPresent(SubscribeToUser.class)) {
                Object target = instanceGetter.getInstance(aClass);
                for (UserInvolvement involvement : method.getAnnotation(SubscribeToUser.class).involvement()) {
                    List<UserEventCallback> userSubscribers = this.userSubscribers.get(involvement);
                    userSubscribers.add(new UserEventCallback(target, method, method.getAnnotation(SubscribeToUser.class),involvement));
                }
            }
        }
    }
    
    public void broadcast(ScopeEvent event) {
        for (ScopedCallback callback : scopeSubscribers.get(event.getScopePhase())) {
            if (callback.isMatch(event)) {
                callback.invoke(event, event.getScopePhase());
            }
        }
    }

    public void broadcast(UserEvent event) {
        for (UserEventCallback callback : userSubscribers.get(event.getInvolvement())) {
            if (callback.isMatch(event)) {
                callback.invoke(event, event.getInvolvement());
            }
        }
    }
}
