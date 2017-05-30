package com.sbg.bdd.screenplay.core.util;

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

    @Override
    public abstract boolean equals(Object object);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();


}
