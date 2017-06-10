package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.resource.file.RootDirectoryResource
import com.sbg.bdd.screenplay.core.actors.Performance
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus
import com.sbg.bdd.screenplay.core.internal.BaseCastingDirector
import com.sbg.bdd.screenplay.core.internal.SimpleInstanceGetter
import com.sbg.bdd.screenplay.core.persona.properties.PropertiesPersonaClient
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient
import com.sbg.bdd.wiremock.scoped.recording.endpointconfig.RemoteEndPointConfigRegistry
import groovy.json.JsonOutput
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import spock.lang.Specification

abstract class WhenWorkingWithWireMock extends Specification{
    public static final int MAX_LEVELS = 10;
    public static final int PRIORITIES_PER_LEVEL = 10;
    static final int EVERYBODY_PRIORITY_DECREMENT = PRIORITIES_PER_LEVEL / 2;
    def initializeWireMock(GlobalScope globalScope) {
        def wireMockServer = new ScopedWireMockServer()
        globalScope.remember(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT, new RecordingWireMockClient(wireMockServer))
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
        def inputResourceRoot = new RootDirectoryResource(markerFile.getParentFile())
        def outputResourceDir = new File(markerFile.getParentFile().getParent(), "output_resource_root")
        outputResourceDir.mkdirs();
        def outputResourceRoot = new RootDirectoryResource(outputResourceDir)
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
                            [propertyName: 'external.service.a', url: 'http://somehost.com/service/one/endpoint', endpointType: 'REST'],
                            [propertyName: 'external.service.b', url: 'http://somehost.com/service/two/endpoint', endpointType: 'SOAP']

                    ]])
                } else {
                    body = JsonOutput.toJson([propertyName: 'x', url: 'http://somehost.com/resolved/endpoint', endpointType: 'SOAP'])
                }
                return Mock(Call) {
                    execute() >>{
                        new Response.Builder().request(request).body(ResponseBody.create(MediaType.parse("application/json"), body)).code(200).protocol(Protocol.HTTP_2).message("OK").build();
                    }
                }
            }
        }
        def globalScope = new GlobalScope(name, castingDirector, eventBus)
        globalScope.remember(WireMockScreenplayContext.ENDPOINT_CONFIG_REGISTRY, new RemoteEndPointConfigRegistry(httpMock, 'http://localhost:8080/base'))
        globalScope.remember(WireMockScreenplayContext.PERSONA_CLIENT, personaClient)
        globalScope.remember('runId', runId)

        globalScope.remember(Performance.OUTPUT_RESOURCE_ROOT, outputResourceRoot)
        globalScope.remember(Performance.INPUT_RESOURCE_ROOT, inputResourceRoot)
        globalScope.remember(WireMockScreenplayContext.JOURNAL_RESOURCE_ROOT, new RootDirectoryResource(new File('build', 'journal')))
        globalScope.remember(WireMockScreenplayContext.BASE_URL_OF_SERVICE_UNDER_TEST, 'http://service.com/under/test')
        globalScope
    }
}
