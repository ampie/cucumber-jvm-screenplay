package com.sbg.bdd.screenplay.scoped

import com.sbg.bdd.resource.file.DirectoryResourceRoot
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus
import com.sbg.bdd.screenplay.core.internal.PersonaBasedCast
import com.sbg.bdd.screenplay.core.internal.SimpleInstanceGetter
import com.sbg.bdd.screenplay.core.persona.properties.PropertiesPersonaClient
import spock.lang.Specification

abstract class WhenUsingScopes extends Specification {

    def buildGlobalScope(String name, Class<?>... glue) {
        def markerFile = new File(Thread.currentThread().contextClassLoader.getResource('screenplay-scoped-marker.txt').file)
        def inputResourceRoot = new DirectoryResourceRoot('inputRoot', markerFile.getParentFile())
        def eventBus = new ScreenPlayEventBus(new SimpleInstanceGetter())
        def cast = new PersonaBasedCast(eventBus, new PropertiesPersonaClient(), inputResourceRoot)
        def classes = new HashSet<Class<?>>(Arrays.asList(glue))
        eventBus.scanClasses(classes)
        def globalScope = new GlobalScope(name, cast, eventBus)
        globalScope.start()
        globalScope
    }

}
