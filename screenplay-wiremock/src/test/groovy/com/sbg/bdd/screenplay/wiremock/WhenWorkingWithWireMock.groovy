package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.resource.file.RootDirectoryResource
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus
import com.sbg.bdd.screenplay.core.internal.BaseCastingDirector
import com.sbg.bdd.screenplay.core.internal.SimpleInstanceGetter
import com.sbg.bdd.screenplay.core.persona.properties.PropertiesPersonaClient
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.wiremock.scoped.ScopedWireMockServer
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient
import com.sbg.bdd.wiremock.scoped.recording.endpointconfig.RemoteEndPointConfigRegistry
import groovy.json.JsonOutput
import org.apache.http.ProtocolVersion
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine
import spock.lang.Specification

import java.io.File

abstract class WhenWorkingWithWireMock extends Specification{
    public static final int MAX_LEVELS = 10;
    public static final int PRIORITIES_PER_LEVEL = 10;
    static final int EVERYBODY_PRIORITY_DECREMENT = PRIORITIES_PER_LEVEL / 2;
    def initializeWireMock(GlobalScope globalScope) {
        def wireMockServer = new ScopedWireMockServer()
        globalScope.remember('recordingWireMockClient', new RecordingWireMockClient(wireMockServer))
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
        def httpMock = Mock(CloseableHttpClient) {
            execute(_) >> { args ->
                HttpUriRequest request = args[0]
                def body = null;
                if (request.getURI().getPath().endsWith('/Property/all')) {
                    body = JsonOutput.toJson([configs: [
                            [propertyName: 'external.service.a', url: 'http://somehost.com/service/one/endpoint', endpointType: 'REST'],
                            [propertyName: 'external.service.b', url: 'http://somehost.com/service/two/endpoint', endpointType: 'SOAP']

                    ]])
                } else {
                    body = JsonOutput.toJson([propertyName: 'x', url: 'http://somehost.com/resolved/endpoint', endpointType: 'SOAP'])
                }
                return Mock(CloseableHttpResponse) {
                    getEntity() >> new StringEntity(body, ContentType.APPLICATION_JSON)
                    getStatusLine() >> new BasicStatusLine(new ProtocolVersion('http', 2, 0), 200, 'OK')
                }
            }
        }
        def globalScope = new GlobalScope(name, castingDirector, eventBus)
        globalScope.remember('endpointConfigRegistry', new RemoteEndPointConfigRegistry(httpMock, 'http://localhost:8080/base'))
        globalScope.remember('personaClient', personaClient)
        globalScope.remember('runId', runId)

        globalScope.remember('outputResourceRoot', outputResourceRoot)
        globalScope.remember('inputResourceRoot', inputResourceRoot)
        globalScope.remember('journalRoot', new RootDirectoryResource(new File('build', 'journal')))
        globalScope.remember('baseUrlOfServiceUnderTest', 'http://service.com/under/test')
        globalScope
    }
}
