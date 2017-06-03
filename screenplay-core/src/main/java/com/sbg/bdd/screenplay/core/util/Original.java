package com.sbg.bdd.screenplay.core.util;

public class Original {
    public static <T> Class<T> versionOf(Class<T> questionClass) {
        return questionClass.getName().contains("EnhancerByCGLIB") ? (Class<T>) questionClass.getSuperclass() : questionClass;
    }
}
