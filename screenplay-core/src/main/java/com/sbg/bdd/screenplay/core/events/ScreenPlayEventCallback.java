package com.sbg.bdd.screenplay.core.events;

import com.sbg.bdd.screenplay.core.util.NameConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.regex.Pattern;

public abstract class ScreenPlayEventCallback {
    private Method method;
    private String namePattern;
    private Object target;
    private int level;

    protected ScreenPlayEventCallback(Object target, Method method,String namePattern, int level) {
        this.target = target;
        this.method = method;
        this.namePattern = namePattern;
        this.level = level;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getDeclaringClass(){
        if(method.getDeclaringClass().isInterface()){
            if(target.getClass().getEnclosingClass()!=null) {
                return target.getClass().getEnclosingClass();
            }else{
                return target.getClass();
            }
        }else{
            return method.getDeclaringClass();
        }
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
    protected boolean levelsMatch(int levelToMatch) {
        return level == -1 || levelToMatch == level;
    }
}
