package com.sbg.bdd.screenplay.scoped

import com.sbg.bdd.resource.file.RootDirectoryResource
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus
import com.sbg.bdd.screenplay.core.internal.BaseCastingDirector
import com.sbg.bdd.screenplay.core.internal.SimpleInstanceGetter
import com.sbg.bdd.screenplay.core.persona.properties.PropertiesPersonaClient
import spock.lang.Specification

abstract class WhenUsingScopes extends Specification {

    def buildGlobalScope(String name, Class<?>... glue) {
        def markerFile = new File(Thread.currentThread().contextClassLoader.getResource('screenplay-scoped-marker.txt').file)
        def inputResourceRoot = new RootDirectoryResource(markerFile.getParentFile())
        def eventBus = new ScreenPlayEventBus(new SimpleInstanceGetter())
        def castingDirector = new BaseCastingDirector(eventBus, new PropertiesPersonaClient(), inputResourceRoot)
        def classes = new HashSet<Class<?>>(Arrays.asList(glue))
        eventBus.scanClasses(classes)
        def globalScope = new GlobalScope(name, castingDirector, eventBus)
        globalScope.start()
        globalScope
    }

}
