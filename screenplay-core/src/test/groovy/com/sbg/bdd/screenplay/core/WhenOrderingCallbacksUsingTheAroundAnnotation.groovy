package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.annotations.Around
import com.sbg.bdd.screenplay.core.annotations.Within
import com.sbg.bdd.screenplay.core.events.CallbackNestingSequence
import spock.lang.Specification

class WhenOrderingCallbacksUsingTheAroundAnnotation extends Specification {
    @Around(Class2)
    @Within(Conflict)
    static class Class1{

    }
    @Around(Class3)
    static class Class2{

    }
    @Around(Conflict)
    static class Class3{

    }
    @Around(Circular2)
    static class Circular1{

    }
    @Around(Circular3)
    static class Circular2{

    }
    @Around(Circular1)
    static class Circular3{

    }
    @Within(Class2)
    static class Conflict{

    }
    def 'should transitively place classes that are annotatede to be around others first' () {
        when:

        def result = CallbackNestingSequence.compareDeclaringClasses(Class1,Class3)

        then:
        result < 0
    }
    def 'should reverse order classes' () {
        when:

        def result = CallbackNestingSequence.compareDeclaringClasses(Class3,Class1)

        then:
        result > 0
    }
    def 'should fail on circular arounds' () {
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
