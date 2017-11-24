package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.resource.file.DirectoryResourceRoot
import com.sbg.bdd.screenplay.core.actors.Performance
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus
import com.sbg.bdd.screenplay.core.internal.PersonaBasedCast
import com.sbg.bdd.screenplay.core.internal.SimpleInstanceGetter
import com.sbg.bdd.screenplay.core.persona.properties.PropertiesPersonaClient
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.screenplay.wiremock.listeners.ScopeManagementListener
import com.sbg.bdd.wiremock.scoped.admin.model.GlobalCorrelationState
import com.sbg.bdd.wiremock.scoped.integration.BaseDependencyInjectorAdaptor
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import groovy.json.JsonOutput
import okhttp3.*
import spock.lang.Specification

abstract class WhenWorkingWithWireMock extends Specification{
    public static final int MAX_LEVELS = 10;
    public static final int PRIORITIES_PER_LEVEL = 10;

    static final int EVERYBODY_PRIORITY_DECREMENT = PRIORITIES_PER_LEVEL / 2;
    def publicAddress = 'http://10.0.0.1:9191'
    def publicIp = '10.0.0.1'
    def publicPort = '9191'
    def markerFile = new File(Thread.currentThread().contextClassLoader.getResource("screenplay-wiremock-marker.txt").file)

    def initializeWireMock(GlobalScope globalScope) {
        def wireMockServer = new ScopedWireMockServer()
//        publicAddress = new IpHelper().findFirstNonExcludedNetworkInterface()

        WireMockMemories.rememberFor(globalScope).toUseWireMock(wireMockServer).withPublicAddress(publicAddress)
        wireMockServer.registerResourceRoot(Performance.OUTPUT_RESOURCE_ROOT,globalScope.recall(Performance.OUTPUT_RESOURCE_ROOT))
        wireMockServer.registerResourceRoot(Performance.INPUT_RESOURCE_ROOT,globalScope.recall(Performance.INPUT_RESOURCE_ROOT))
        wireMockServer.start()
        globalScope.start()
        wireMockServer

    }

    def buildGlobalScope(String name, Class<?>... glue) {
        DependencyInjectionAdaptorFactory.useAdaptor(new BaseDependencyInjectorAdaptor())
        if(glue.length ==0){
            glue=[ScopeManagementListener]
        }
        GlobalScope globalScope = buildGlobalScopeWithoutStarting(name, glue)
        globalScope
    }

    def  buildGlobalScopeWithoutStarting(String name, Class<?>... glue) {
        def inputResourceRoot = new DirectoryResourceRoot("inputRoot", markerFile.getParentFile())
        def outputResourceDir = new File(markerFile.getParentFile().getParent(), "output_resource_root")
        outputResourceDir.mkdirs();
        def outputResourceRoot = new DirectoryResourceRoot("outputRoot", outputResourceDir)
        def eventBus = new ScreenPlayEventBus(new SimpleInstanceGetter())
        def personaClient = new PropertiesPersonaClient()
        def cast = new PersonaBasedCast(eventBus, personaClient, inputResourceRoot)
        eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(glue)))
        def globalScope = new GlobalScope(name, cast, eventBus)
        def remember = WireMockMemories.rememberFor(globalScope)
        remember
            .toUseThePersonaClient(personaClient)
            .toWriteResourcesTo(outputResourceRoot)
            .toReadResourcesFrom(inputResourceRoot)
            .toUseThePersonasAt(inputResourceRoot)
            .toUseTheJournalAt(new DirectoryResourceRoot('journalRoot', new File('build', 'journal')))
            .toPointTo('http://service.com/under/test')
        globalScope.remember(WireMockScreenplayContext.CORRELATION_STATE,new GlobalCorrelationState('name',remember.theBaseUrlOfTheServiceUnderTest(),remember.theBaseUrlOfTheServiceUnderTest(),remember.theIntegrationScope()))
        globalScope
    }
}
