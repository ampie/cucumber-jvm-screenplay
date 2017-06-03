package com.sbg.bdd.screenplay.core.events;

import com.sbg.bdd.screenplay.core.annotations.Around;
import com.sbg.bdd.screenplay.core.annotations.Within;

import java.util.Comparator;

public class CallbackNestingSequence implements Comparator<ScreenPlayEventCallback> {
    @Override
    public int compare(ScreenPlayEventCallback a, ScreenPlayEventCallback b) {
        Class<?> classA = a.getDeclaringClass();
        Class<?> classB = b.getDeclaringClass();
        if (classA != classB) {
            int x = compareDeclaringClasses(classA, classB);
            if (x != 0) {
                return x;
            }
        }
        return a.getMethod().toGenericString().compareTo(b.getMethod().toGenericString());
    }

    public static int compareDeclaringClasses(Class<?> classA, Class<?> classB) {
        if (isAWithinB(classA, classB)) {
            if (isAWithinB(classB, classA) || isAAroundB(classA, classB)) {
                throw new IllegalArgumentException();
            } else {
                return 1;
            }
        } else if (isAWithinB(classB, classA)) {
            if (isAWithinB(classA, classB) || isAAroundB(classB, classA)) {
                throw new IllegalArgumentException();
            } else {
                return -1;
            }
        } else if (isAAroundB(classA, classB)) {
            if (isAAroundB(classB, classA) || isAWithinB(classA,classB) ) {
                throw new IllegalArgumentException();
            } else {
                return -1;
            }
        } else if (isAAroundB(classB, classA)) {
            if (isAAroundB(classA, classB) || isAWithinB(classB,classA) ) {
                throw new IllegalArgumentException();
            } else {
                return 1;
            }
        }
        return 0;
    }

    private static boolean isAAroundB(Class<?> a, Class<?> b) {
        return a.isAnnotationPresent(Around.class) && (a.getAnnotation(Around.class).value().equals(b) || isAAroundB(a.getAnnotation(Around.class).value(), b));
    }

    private static boolean isAWithinB(Class<?> a, Class<?> b) {
        return a.isAnnotationPresent(Within.class) && (a.getAnnotation(Within.class).value().equals(b) || isAWithinB(a.getAnnotation(Within.class).value(), b));
    }
}
