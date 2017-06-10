package com.sbg.bdd.screenplay.core.internal;

import com.sbg.bdd.screenplay.core.*;
import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.screenplay.core.util.AnnotatedTitle;
import com.sbg.bdd.screenplay.core.util.NameConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class StepMethodInfo {
    private String stepParentPath;
    private String methodName;
    protected final Object performer;
    protected String location;
    protected String nameExpression;
    protected Object origin;
    private boolean pending;
    private boolean ignored;
    private String name;
    private Method method;

    protected StepMethodInfo(String stepParentPath, String methodName, Object performer, Object origin) {
        this.stepParentPath = stepParentPath;
        this.methodName = methodName;
        this.performer = performer;
        this.origin = origin;
        deriveState();
    }

    public String getStepPath() {
        return stepParentPath == null ? getName() : stepParentPath + "/" + NameConverter.filesystemSafe(getName());
    }

    public String getKeyword() {
        return methodName;
    }

    public String getNameExpression() {
        return nameExpression;
    }

    public String getName() {
        return name;
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isSkipped() {
        return ignored;
    }

    protected void deriveState() {
        method = lookupMethod();
        extractNameExpressionFrom();
        locateMethod();
        this.name = AnnotatedTitle.injectFieldsInto(nameExpression).using(getImplementation());
        if (getImplementation() instanceof Question) {
            name = origin.toString();
        }
        pending = isAnnotationPresent(method, "PendingStep");
        ignored = isAnnotationPresent(method, "Ignore");
    }

    public Object getImplementation() {
        return origin instanceof Consequence ? ((Consequence) origin).getQuestion() : origin;
    }

    private void locateMethod() {
        this.location = method.getDeclaringClass().getSimpleName() + " " + method.getReturnType().getSimpleName() + " " + method.getName();
    }


    protected void extractNameExpressionFrom() {
        this.nameExpression = method.getName();
        if (method.isAnnotationPresent(Step.class)) {
            if (method.getAnnotation(Step.class).value().length() > 0) {
                this.nameExpression = method.getAnnotation(Step.class).value();
            }
        }
    }

    private Method lookupMethod() {
        Method[] methods = origin.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException(getImplementation().getClass() + " does not have method " + methodName);
    }

    private boolean isAnnotationPresent(Method method, String... annotationNames) {
        for (Annotation annotation : method.getAnnotations()) {
            for (String annotationName : annotationNames) {
                if (annotation.getClass().getInterfaces()[0].getSimpleName().equals(annotationName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void invoke() {
        try {
            lookupMethod().invoke(origin, performer);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            } else if (e.getTargetException() instanceof Error) {
                throw (Error) e.getTargetException();
            } else {
                throw new IllegalStateException(e.getTargetException());
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getLocation() {
        return location;
    }

    public int getStepLevel() {
        return stepParentPath == null ? 0 : stepParentPath.split("\\/").length;
    }
}
