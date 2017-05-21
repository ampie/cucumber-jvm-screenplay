package cucumber.screenplay.util;

import java.util.Iterator;
import java.util.Set;

public abstract class Optional<T> {


    public static <T> Optional<T> absent() {
        return Absent.withType();
    }


    public static <T> Optional<T> of(T reference) {
        return new Present<T>(checkNotNull(reference));
    }

    protected static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static <T> Optional<T> fromNullable(T nullableReference) {
        return (nullableReference == null)
                ? Optional.<T>absent()
                : new Present<T>(nullableReference);
    }


    Optional() {
    }

    public abstract boolean isPresent();


    public abstract T get();


    public abstract T or(T defaultValue);


    public abstract Optional<T> or(Optional<? extends T> secondChoice);



    public abstract T orNull();

    public abstract Set<T> asSet();



    public java.util.Optional<T> toJavaUtil() {
        return java.util.Optional.ofNullable(orNull());
    }


    @Override
    public abstract boolean equals(Object object);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();


}
