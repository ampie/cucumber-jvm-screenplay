package cucumber.screenplay.internal;

import cucumber.screenplay.*;
import cucumber.screenplay.util.AnnotatedTitle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


public abstract class ChildStepInfo {
    protected final Object performer;
    protected String location;
    protected String name;
    protected Object origin;
    private boolean pending;
    private boolean ignored;

    protected ChildStepInfo(Object performer, Object origin) {
        this.performer = performer;
        this.origin = origin;
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isSkipped() {
        return ignored;
    }

    protected void deriveState() throws NoSuchMethodException {
        Method method = lookupMethod();
        extractNameFrom(method);
        locateMethod(method);
        this.name = AnnotatedTitle.injectFieldsInto(name).using(getImplementation());
        if (getImplementation() instanceof Question) {
            name = origin.toString();
        }
        pending = isAnnotationPresent(method, "PendingStep");
        ignored = isAnnotationPresent(method, "Ignore");
    }

    public Object getImplementation() {
        return origin instanceof Consequence ? ((Consequence) origin).getQuestion():origin;
    }

    private void locateMethod(Method method) {
        this.location = method.getDeclaringClass().getSimpleName() + " " + method.getReturnType().getSimpleName() + " " + method.getName();
    }



    protected void extractNameFrom(Method method) {
        this.name = method.getName();
        if (method.isAnnotationPresent(cucumber.screenplay.annotations.Step.class)) {
            if (method.getAnnotation(cucumber.screenplay.annotations.Step.class).value().length() > 0) {
                name = method.getAnnotation(cucumber.screenplay.annotations.Step.class).value();
            }
        }
    }

    private Method lookupMethod() throws NoSuchMethodException {
        if (getImplementation() instanceof Performable) {
            return getImplementation().getClass().getMethod("performAs", Actor.class);
        } else if (getImplementation() instanceof Question) {
            return getImplementation().getClass().getMethod("answeredBy", Actor.class);
        } else if(getImplementation() instanceof OnStageAction){
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
}
