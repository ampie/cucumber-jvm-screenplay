package cucumber.screenplay.formatter;

import cucumber.screenplay.*;
import cucumber.screenplay.util.AnnotatedTitle;
import cucumber.screenplay.util.Fields;
import cucumber.screenplay.util.StringConverter;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;


public class ChildStepInfo {
    private final Object performer;
    private List<Argument> arguments;
    private String location;
    private String name;
    private Object origin;
    private Step step;
    private Match match;
    private boolean pending;
    private boolean ignored;

    public ChildStepInfo(String keyword, Object origin, Object performer) {
        try {
            this.performer = performer;
            this.origin = origin;
            deriveState();
            step = new Step(null, keyword, name, null, null, null);
            match = new Match(arguments, location);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public Step getStep() {
        return step;
    }

    public Match getMatch() {
        return match;
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isSkipped() {
        return ignored;
    }

    private void deriveState() throws NoSuchMethodException {
        Method method = lookupMethod();
        extractNameFrom(method);
        buildArguments();
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

    private void buildArguments() {
        this.arguments = new ArrayList<>();
        if (name.contains("#")) {
            Scanner scanner = new Scanner(name);
            scanner.useDelimiter(Pattern.compile("[^\\w\\#]"));
            while (scanner.hasNext()) {
                String next = scanner.next();
                if (next.startsWith("#")) {
                    Object fieldValue = Fields.of(getImplementation()).asMap().get(next.substring(1));
                    String stringValue = StringConverter.toString(fieldValue);
                    arguments.add(new Argument(null, stringValue));
                }
            }
        }
    }

    private void extractNameFrom(Method method) {
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
