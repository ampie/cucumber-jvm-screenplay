package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.util.OuterClass
import spock.lang.Specification

class WhenFindingAListenersOuterClass extends Specification{
    //Lambda fake
    class $$Lambda{

    }
    class X{

    }

    def 'it should find the declaring class of an inner class'(){
        when:
        def cls = OuterClass.of(X.class)
        then:
        cls == WhenFindingAListenersOuterClass
    }
    def 'it should find the enclosing class of an anonymous class'(){
        when:
        def cls = OuterClass.of(new Object(){}.getClass())
        then:
        cls == WhenFindingAListenersOuterClass
    }
    def 'it should find the enclosing class of an java 8 lambda'(){
        when:
        def cls = OuterClass.of($$Lambda)
        then:
        cls == WhenFindingAListenersOuterClass
    }
}
