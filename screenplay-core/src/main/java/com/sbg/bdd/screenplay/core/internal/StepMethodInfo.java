package com.sbg.bdd.screenplay.core.internal;

import com.sbg.bdd.screenplay.core.*;
import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.screenplay.core.util.AnnotatedTitle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


public class StepMethodInfo {
    private String stepParentPath;
    private String keyword;
    protected final Object performer;
    protected String location;
    protected String nameExpression;
    protected Object origin;
    private boolean pending;
    private boolean ignored;
    private String name;

    protected StepMethodInfo(String stepParentPath, String keyword, Object performer, Object origin) {
        this.stepParentPath = stepParentPath;
        this.keyword = keyword;
        this.performer = performer;
        this.origin = origin;
        try {
            deriveState();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getStepPath() {
        return stepParentPath==null?getName():stepParentPath+"/"+getName();
    }

    public String getKeyword() {
        return keyword;
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

    protected void deriveState() throws NoSuchMethodException {
        Method method = lookupMethod();
        extractNameExpressionFrom(method);
        locateMethod(method);
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

    private void locateMethod(Method method) {
        this.location = method.getDeclaringClass().getSimpleName() + " " + method.getReturnType().getSimpleName() + " " + method.getName();
    }


    protected void extractNameExpressionFrom(Method method) {
        this.nameExpression = method.getName();
        if (method.isAnnotationPresent(Step.class)) {
            if (method.getAnnotation(Step.class).value().length() > 0) {
                this.nameExpression= method.getAnnotation(Step.class).value();
            }
        }
    }

    private Method lookupMethod() throws NoSuchMethodException {
        if (getImplementation() instanceof Performable) {
            return getImplementation().getClass().getMethod("performAs", Actor.class);
        } else if (getImplementation() instanceof Question) {
            return getImplementation().getClass().getMethod("answeredBy", Actor.class);
        } else if (getImplementation() instanceof OnStageAction) {
            return getImplementation().getClass().getMethod("performOnStage", ActorOnStage.class);
        }
        throw new IllegalStateException(getImplementation().getClass() + " not a question or a task");
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
        if (performer instanceof Actor) {
            if (this.origin instanceof Consequence) {
                ((Consequence) this.origin).evaluateFor((Actor) performer);
            } else if (this.origin instanceof Performable) {
                ((Performable) this.origin).performAs((Actor) performer);
            }
        } else if (performer instanceof ActorOnStage) {
            if (this.origin instanceof OnStageAction) {
                ((OnStageAction) this.origin).performOnStage((ActorOnStage) performer);
            }
        }
    }

    public String getLocation() {
        return location;
    }

    public int getStepLevel() {
        return stepParentPath==null?0:stepParentPath.split("\\/").length;
    }
}
