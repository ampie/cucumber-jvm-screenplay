package com.sbg.bdd.screenplay.core.util;

public class OuterClass {
    public static Class<?> of(Class<?> c) {
        if (c.isSynthetic() && c.getName().contains("$$Lambda")) {
            try {
                return Class.forName(c.getName().substring(0, c.getName().indexOf("$$Lambda")));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        } else if (c.getEnclosingClass() != null) {
            return c.getEnclosingClass();
        } else if (c.getDeclaringClass() != null) {
            return c.getDeclaringClass();
        } else {
            throw new IllegalArgumentException("Is " + c.getName() + " an inner or anonymouos class?");
        }
    }
}
