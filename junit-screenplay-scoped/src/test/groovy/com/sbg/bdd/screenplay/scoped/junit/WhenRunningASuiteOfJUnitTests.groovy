package com.sbg.bdd.screenplay.scoped.junit

import com.sbg.bdd.screenplay.core.annotations.SceneEventType
import org.junit.runner.JUnitCore
import spock.lang.Specification

class WhenRunningASuiteOfJUnitTests extends Specification {
    def 'should generate events for the globalScope was started and all the tests belonging to the suite'() {
        given: 'a JUnit test suite that is annotated to run with the ScopedSuite runner'
        def clazz = ScopedTestSuite.class
        ScopedTestSuite.sceneEventList.clear()
        when: 'the suite is executed'
        JUnitCore junit = new JUnitCore()
        junit.run(clazz)
        then: 'events are generated in a normal sequence as if a globalScope was started and stopped'
        ScopedTestSuite.sceneEventList[0].sceneEventType == SceneEventType.BEFORE_START
        ScopedTestSuite.sceneEventList[1].sceneEventType == SceneEventType.AFTER_START
        ScopedTestSuite.sceneEventList[1].scene.sceneIdentifier == ''
        ScopedTestSuite.sceneEventList[2].sceneEventType == SceneEventType.BEFORE_START
        ScopedTestSuite.sceneEventList[3].sceneEventType == SceneEventType.AFTER_START
        ScopedTestSuite.sceneEventList[3].scene.sceneIdentifier == 'com.sbg.bdd.screenplay.scoped.junit.package1'
        ScopedTestSuite.sceneEventList[4].sceneEventType == SceneEventType.BEFORE_START
        ScopedTestSuite.sceneEventList[5].sceneEventType == SceneEventType.AFTER_START
        ScopedTestSuite.sceneEventList[5].scene.sceneIdentifier == 'com.sbg.bdd.screenplay.scoped.junit.package1/ScopedTest1_1'
        ScopedTestSuite.sceneEventList[6].sceneEventType == SceneEventType.BEFORE_START
        ScopedTestSuite.sceneEventList[7].sceneEventType == SceneEventType.AFTER_START
        ScopedTestSuite.sceneEventList[7].scene.sceneIdentifier == 'com.sbg.bdd.screenplay.scoped.junit.package1/ScopedTest1_1/testMe'
        ScopedTestSuite.sceneEventList[8].sceneEventType == SceneEventType.BEFORE_COMPLETE
        ScopedTestSuite.sceneEventList[9].sceneEventType == SceneEventType.AFTER_COMPLETE
        ScopedTestSuite.sceneEventList[9].scene.sceneIdentifier == 'com.sbg.bdd.screenplay.scoped.junit.package1/ScopedTest1_1/testMe'
        ScopedTestSuite.sceneEventList[10].sceneEventType == SceneEventType.BEFORE_COMPLETE
        ScopedTestSuite.sceneEventList[11].sceneEventType == SceneEventType.AFTER_COMPLETE
        ScopedTestSuite.sceneEventList[11].scene.sceneIdentifier == 'com.sbg.bdd.screenplay.scoped.junit.package1/ScopedTest1_1'
        ScopedTestSuite.sceneEventList[12].sceneEventType == SceneEventType.BEFORE_START
        ScopedTestSuite.sceneEventList[13].sceneEventType == SceneEventType.AFTER_START
        ScopedTestSuite.sceneEventList[13].scene.sceneIdentifier == 'com.sbg.bdd.screenplay.scoped.junit.package1/ScopedTest1_2'
        ScopedTestSuite.sceneEventList[14].sceneEventType == SceneEventType.BEFORE_START
        ScopedTestSuite.sceneEventList[15].sceneEventType == SceneEventType.AFTER_START
        ScopedTestSuite.sceneEventList[15].scene.sceneIdentifier == 'com.sbg.bdd.screenplay.scoped.junit.package1/ScopedTest1_2/testMe'
        ScopedTestSuite.sceneEventList[16].sceneEventType == SceneEventType.BEFORE_COMPLETE
        ScopedTestSuite.sceneEventList[17].sceneEventType == SceneEventType.AFTER_COMPLETE
        ScopedTestSuite.sceneEventList[17].scene.sceneIdentifier == 'com.sbg.bdd.screenplay.scoped.junit.package1/ScopedTest1_2/testMe'
        ScopedTestSuite.sceneEventList[18].sceneEventType == SceneEventType.BEFORE_COMPLETE
        ScopedTestSuite.sceneEventList[19].sceneEventType == SceneEventType.AFTER_COMPLETE
        ScopedTestSuite.sceneEventList[19].scene.sceneIdentifier == 'com.sbg.bdd.screenplay.scoped.junit.package1/ScopedTest1_2'
        ScopedTestSuite.sceneEventList[20].sceneEventType == SceneEventType.BEFORE_COMPLETE
        ScopedTestSuite.sceneEventList[21].sceneEventType == SceneEventType.AFTER_COMPLETE
        ScopedTestSuite.sceneEventList[21].scene.sceneIdentifier == 'com.sbg.bdd.screenplay.scoped.junit.package1'



    }
}
