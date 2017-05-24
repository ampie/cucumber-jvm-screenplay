package cucumber.screenplay;

import cucumber.screenplay.annotations.Subject;
import cucumber.screenplay.util.AnnotatedTitle;
import cucumber.screenplay.util.Optional;
import cucumber.screenplay.util.Uninstrumented;

import java.lang.reflect.Method;


import static cucumber.screenplay.util.NameConverter.humanize;
import static java.beans.Introspector.decapitalize;
import static org.apache.commons.lang3.StringUtils.lowerCase;

public class QuestionSubject<T> {

    private final Class<? extends Question> questionClass;
    private Question<T> question;

    @SuppressWarnings("unchecked")
    public static <T> QuestionSubject<T> fromClass(Class<?> questionClass) {
        return new QuestionSubject(questionClass);
    }

    public QuestionSubject(Class<? extends Question> questionClass) {
        this.questionClass = Uninstrumented.versionOf(questionClass);
    }

    public QuestionSubject andQuestion(Question question) {
        this.question = question;
        return this;
    }

    private Optional<String> annotatedSubject() {
        if (annotationOnMethodOf(questionClass).isPresent()) {
            return Optional.of(annotationOnMethodOf(questionClass)).get();
        }
        return annotatedSubjectFromClass(questionClass);
    }

    private Optional<String> annotatedSubjectFromClass(Class<?> questionClass) {
        if (questionClass.getAnnotation(Subject.class) != null) {
            return Optional.of(annotationOnClass(questionClass)).get();
        }

        if (questionClass.getSuperclass() != null) {
            return annotatedSubjectFromClass(questionClass.getSuperclass());
        }

        return Optional.absent();
    }

    private Optional<String> annotationOnMethodOf(Class<? extends Question> questionClass) {
        try {
            Method answeredBy = questionClass.getMethod("answeredBy", Actor.class);
            if (answeredBy.getAnnotation(Subject.class) != null) {
                String annotatedTitle = answeredBy.getAnnotation(Subject.class).value();
                annotatedTitle = AnnotatedTitle.injectFieldsInto(annotatedTitle).using(question);
                return Optional.of(annotatedTitle);
            }
        } catch (NoSuchMethodException e) {
            return Optional.absent();
        }
        return Optional.absent();
    }

    private Optional<String> annotationOnClass(Class<?> questionClass) {
        if (questionClass.getAnnotation(Subject.class) != null) {
            String annotatedTitle = questionClass.getAnnotation(Subject.class).value();
            annotatedTitle = AnnotatedTitle.injectFieldsInto(annotatedTitle).using(question);
            return Optional.of(annotatedTitle);
        }
        return Optional.absent();
    }

    public String subject() {
        return annotatedSubject().or(decapitalize(humanize(questionClass.getSimpleName())));
    }

}
