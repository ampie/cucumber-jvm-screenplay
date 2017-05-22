package cucumber.scoping.events;

import cucumber.scoping.annotations.SubscribeToScope;
import cucumber.scoping.annotations.SubscribeToUser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScopeEventBus {
    
    private InstanceGetter objectFactory;
    private List<ScopedCallback> scopeSubscribers = new ArrayList<>();
    private List<UserEventCallback> userSubscribers = new ArrayList<>();
    private int featureLevel;
    
    public ScopeEventBus(InstanceGetter objectFactory, int featureLevel) {
        this.objectFactory = objectFactory;
        this.featureLevel = featureLevel;
    }

    public static Class<?> mostSpecific(Method method, Class<?> aClass) {
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

    public static void invoke(Object target, Object scope, Enum<?> phase, Method method) throws IllegalAccessException, InvocationTargetException {
        if (method.getParameterTypes().length == 0) {
            method.invoke(target);
        } else if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isInstance(scope)) {
            method.invoke(target, scope);
        } else if (method.getParameterTypes().length == 2 && method.getParameterTypes()[0].isInstance(scope) && method.getParameterTypes()[1].isInstance(phase)) {
            method.invoke(target, scope, phase);
        }
    }
    
    public int getFeatureLevel() {
        return featureLevel;
    }
    
    public void scanClasses(Set<Class<?>> classes) {
        for (Class<?> aClass : classes) {
            scanMethods(aClass);
        }
    }
    
    
    private void scanMethods(Class<?> aClass) {
        for (Method method : aClass.getMethods()) {
            if (method.isAnnotationPresent(SubscribeToScope.class)) {
                Object target = objectFactory.getInstance(aClass);
                scopeSubscribers.add(new ScopedCallback(target, method, method.getAnnotation(SubscribeToScope.class)));
            }
            if (method.isAnnotationPresent(SubscribeToUser.class)) {
                Object target = objectFactory.getInstance(aClass);
                userSubscribers.add(new UserEventCallback(target, method, method.getAnnotation(SubscribeToUser.class)));
            }

        }
    }
    
    public void broadcast(ScopeEvent event) {
        for (ScopedCallback callback : scopeSubscribers) {
            if (callback.isMatch(event)) {
                callback.execute(event.getScope(), event.getScopePhase());
            }
        }
    }

    public void broadcast(UserEvent event) {
        for (UserEventCallback callback : userSubscribers) {
            if (callback.isMatch(event)) {
                callback.execute(event.getUserInScope(), event.getInvolvement());
            }
        }
    }
}
