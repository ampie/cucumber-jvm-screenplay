package com.sbg.bdd.screenplay.core

import com.sbg.bdd.resource.file.DirectoryResourceRoot
import com.sbg.bdd.screenplay.core.internal.BasePerformance
import spock.lang.Specification

abstract class WithinBasePerformance extends Specification{
    def buildPerformance(){
        def markerFile = new File(Thread.currentThread().contextClassLoader.getResource('screenplay-core-marker.txt').file)
        def inputResourceRoot = new DirectoryResourceRoot('inputRoot', markerFile.getParentFile())
        return new BasePerformance('RunIt', inputResourceRoot)

    }
}
