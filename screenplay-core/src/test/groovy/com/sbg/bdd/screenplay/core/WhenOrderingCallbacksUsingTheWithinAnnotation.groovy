package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.annotations.Around
import com.sbg.bdd.screenplay.core.annotations.Within
import com.sbg.bdd.screenplay.core.events.CallbackNestingSequence
import spock.lang.Specification

class WhenOrderingCallbacksUsingTheWithinAnnotation extends Specification {
    @Within(Class2)
    @Around(Conflict)
    static class Class1{

    }
    @Within(Class3)
    static class Class2{

    }
    @Within(Conflict)
    static class Class3{

    }
    @Within(Circular2)
    static class Circular1{

    }
    @Within(Circular3)
    static class Circular2{

    }
    @Within(Circular1)
    static class Circular3{

    }
    @Around(Class2)
    static class Conflict{

    }
    def 'should transitively place classes that are annotated to be within others last' () {
        when:

        def result = CallbackNestingSequence.compareDeclaringClasses(Class1,Class3)

        then:
        result > 0
    }
    def 'should reverse order classes' () {
        when:

        def result = CallbackNestingSequence.compareDeclaringClasses(Class3,Class1)

        then:
        result < 0
    }
    def 'should fail on circular withins' () {
        when:
        def exception = null
        try {
            CallbackNestingSequence.compareDeclaringClasses(Circular1,Circular3)
        } catch (IllegalArgumentException e) {
            exception=e;
        }

        then:
        exception !=null
    }
    def 'should fail on conflicting arounds and withins forward' () {
        when:
        def exception = null
        try {
            CallbackNestingSequence.compareDeclaringClasses(Class1,Conflict)
        } catch (IllegalArgumentException e) {
            exception=e;
        }

        then:
        exception !=null
    }
    def 'should fail on conflicting arounds and withins backward' () {
        when:
        def exception = null
        try {
            CallbackNestingSequence.compareDeclaringClasses(Conflict,Class1)
        } catch (IllegalArgumentException e) {
            exception=e;
        }

        then:
        exception !=null
    }
}
