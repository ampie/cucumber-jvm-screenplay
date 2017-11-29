package com.sbg.bdd.screenplay.scoped.junit

import com.sbg.bdd.screenplay.core.annotations.SceneEventType
import org.junit.runner.JUnitCore
import spock.lang.Specification

class WhenRunningASingleJUnitTest extends Specification{
    def 'should generate events in a normal sequence as if a globalScope was started and stopped'(){
        given: 'a JUnit test that has the ScopingRule configured and can be run in isolation'
        def clazz = IndividualScopedTest.class
        IndividualScopedTest.sceneEventList.clear()
        when: 'the test is executed'
        JUnitCore junit = new JUnitCore()
        junit.run(clazz)
        def runHooks = Class.forName('java.lang.ApplicationShutdownHooks').getDeclaredMethod('runHooks')
        runHooks.setAccessible(true)
        runHooks.invoke(null)
        then: 'events are generated in a normal sequence as if a globalScope was started and stopped'
        IndividualScopedTest.sceneEventList[0].sceneEventType==SceneEventType.BEFORE_START
        IndividualScopedTest.sceneEventList[1].sceneEventType==SceneEventType.AFTER_START
        IndividualScopedTest.sceneEventList[1].scene.sceneIdentifier==''
        IndividualScopedTest.sceneEventList[2].sceneEventType==SceneEventType.BEFORE_START
        IndividualScopedTest.sceneEventList[3].sceneEventType==SceneEventType.AFTER_START
        IndividualScopedTest.sceneEventList[3].scene.sceneIdentifier=='com.sbg.bdd.screenplay.scoped.junit'
        IndividualScopedTest.sceneEventList[4].sceneEventType==SceneEventType.BEFORE_START
        IndividualScopedTest.sceneEventList[5].sceneEventType==SceneEventType.AFTER_START
        IndividualScopedTest.sceneEventList[5].scene.sceneIdentifier=='com.sbg.bdd.screenplay.scoped.junit/IndividualScopedTest'
        IndividualScopedTest.sceneEventList[6].sceneEventType==SceneEventType.BEFORE_START
        IndividualScopedTest.sceneEventList[7].sceneEventType==SceneEventType.AFTER_START
        IndividualScopedTest.sceneEventList[7].scene.sceneIdentifier=='com.sbg.bdd.screenplay.scoped.junit/IndividualScopedTest/testMe'
        IndividualScopedTest.sceneEventList[8].sceneEventType==SceneEventType.BEFORE_COMPLETE
        IndividualScopedTest.sceneEventList[9].sceneEventType==SceneEventType.AFTER_COMPLETE
        IndividualScopedTest.sceneEventList[9].scene.sceneIdentifier=='com.sbg.bdd.screenplay.scoped.junit/IndividualScopedTest/testMe'
        IndividualScopedTest.sceneEventList[10].sceneEventType==SceneEventType.BEFORE_COMPLETE
        IndividualScopedTest.sceneEventList[11].sceneEventType==SceneEventType.AFTER_COMPLETE
        IndividualScopedTest.sceneEventList[11].scene.sceneIdentifier=='com.sbg.bdd.screenplay.scoped.junit/IndividualScopedTest'
        IndividualScopedTest.sceneEventList[12].sceneEventType==SceneEventType.BEFORE_COMPLETE
        IndividualScopedTest.sceneEventList[13].sceneEventType==SceneEventType.AFTER_COMPLETE
        IndividualScopedTest.sceneEventList[13].scene.sceneIdentifier=='com.sbg.bdd.screenplay.scoped.junit'
        IndividualScopedTest.sceneEventList[14].sceneEventType==SceneEventType.BEFORE_COMPLETE
        IndividualScopedTest.sceneEventList[15].sceneEventType==SceneEventType.AFTER_COMPLETE

    }
}
