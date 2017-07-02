package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.resource.file.DirectoryResourceRoot
import com.sbg.bdd.screenplay.core.actors.Performance
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus
import com.sbg.bdd.screenplay.core.internal.BaseCastingDirector
import com.sbg.bdd.screenplay.core.internal.SimpleInstanceGetter
import com.sbg.bdd.screenplay.core.persona.properties.PropertiesPersonaClient
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.wiremock.scoped.client.endpointconfig.RemoteEndPointConfigRegistry
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import groovy.json.JsonOutput
import okhttp3.*
import spock.lang.Specification

abstract class WhenWorkingWithWireMock extends Specification{
    public static final int MAX_LEVELS = 10;
    public static final int PRIORITIES_PER_LEVEL = 10;

    static final int EVERYBODY_PRIORITY_DECREMENT = PRIORITIES_PER_LEVEL / 2;
    def publicAddress

    def initializeWireMock(GlobalScope globalScope) {
        def wireMockServer = new ScopedWireMockServer()
        publicAddress = new IpHelper().findFirstNonExcludedNetworkInterface()

        WireMockMemories.rememberFor(globalScope).toUseWireMock(wireMockServer).withPublicAddress(publicAddress)
        wireMockServer.registerResourceRoot(Performance.OUTPUT_RESOURCE_ROOT,globalScope.recall(Performance.OUTPUT_RESOURCE_ROOT))
        wireMockServer.registerResourceRoot(Performance.INPUT_RESOURCE_ROOT,globalScope.recall(Performance.INPUT_RESOURCE_ROOT))
        wireMockServer.start()
        wireMockServer
    }

    def buildGlobalScope(String name, int runId, Class<?> ...glue) {
        GlobalScope globalScope = buildGlobalScopeWithoutStarting(name, runId,glue)
        globalScope.start()
        globalScope
    }

    def  buildGlobalScopeWithoutStarting(String name, int runId,Class<?>... glue) {
        def markerFile = new File(Thread.currentThread().contextClassLoader.getResource("screenplay-wiremock-marker.txt").file)
        def inputResourceRoot = new DirectoryResourceRoot("inputRoot", markerFile.getParentFile())
        def outputResourceDir = new File(markerFile.getParentFile().getParent(), "output_resource_root")
        outputResourceDir.mkdirs();
        def outputResourceRoot = new DirectoryResourceRoot("outputRoot", outputResourceDir)
        def eventBus = new ScreenPlayEventBus(new SimpleInstanceGetter())
        def personaClient = new PropertiesPersonaClient()
        def castingDirector = new BaseCastingDirector(eventBus, personaClient, inputResourceRoot)
        eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(glue)))
        def httpMock = Mock(OkHttpClient) {
            newCall(_) >> { args ->
                Request request = args[0]
                def body = null;
                if (request.url().toString().endsWith('/Property/all')) {
                    body = JsonOutput.toJson([configs: [
                            [propertyName: 'external.service.a', url: 'http://somehost.com/service/one/endpoint', endpointType: 'REST',category:'cat1'],
                            [propertyName: 'external.service.b', url: 'http://somehost.com/service/two/endpoint', endpointType: 'SOAP',category:'cat1']

                    ]])
                } else {
                    body = JsonOutput.toJson([propertyName: 'x', url: 'http://somehost.com/resolved/endpoint', endpointType: 'SOAP',category:'cat1'])
                }
                return Mock(Call) {
                    execute() >>{
                        new Response.Builder().request(request).body(ResponseBody.create(MediaType.parse("application/json"), body)).code(200).protocol(Protocol.HTTP_2).message("OK").build();
                    }
                }
            }
        }
        def globalScope = new GlobalScope(name, castingDirector, eventBus)
        WireMockMemories.rememberFor(globalScope)
            .toUseThePersonaClient(personaClient)
            .theRunId(runId)
            .toWriteResourcesTo(outputResourceRoot)
            .toReadResourcesFrom(inputResourceRoot)
            .toUseTheJournalAt(new DirectoryResourceRoot('journalRoot', new File('build', 'journal')))
            .toPointTo('http://service.com/under/test')
              .toUseTheEndpointConfigRegistry(new RemoteEndPointConfigRegistry(httpMock, 'http://localhost:8080/base'))
        globalScope
    }
}
