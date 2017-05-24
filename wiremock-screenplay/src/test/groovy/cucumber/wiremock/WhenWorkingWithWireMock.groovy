package cucumber.wiremock

import com.github.ampie.wiremock.ScopedWireMockServer
import cucumber.scoping.GlobalScope
import cucumber.screenplay.events.ScreenPlayEventBus
import cucumber.screenplay.internal.InstanceGetter
import cucumber.scoping.persona.local.LocalPersonaClient
import cucumber.screenplay.internal.BaseCastingDirector
import groovy.json.JsonOutput
import org.apache.http.ProtocolVersion
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine
import spock.lang.Specification

import java.nio.file.Paths

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
        def resourceRoot = Paths.get('src/test/resources')
        def eventBus = new ScreenPlayEventBus(Mock(InstanceGetter){
            getInstance(_) >> {args -> args[0].newInstance()}
        })
        def castingDirector = new BaseCastingDirector(eventBus, new LocalPersonaClient(), resourceRoot)
        eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(glue)))
        def httpMock = Mock(CloseableHttpClient){
            execute(_) >>{ args ->
                HttpUriRequest request=args[0]
                def body = null;
                if(request.getURI().getPath().endsWith('/Property/all')){
                    body = JsonOutput.toJson([properties:[
                            [propertyName:'external.service.a', propertyValue:'http://somehost.com/service/one/endpoint'],
                            [propertyName:'external.service.b', propertyValue:'http://somehost.com/service/two/endpoint']

                    ]])
                }else{
                    body = JsonOutput.toJson([propertyValue:'http://somehost.com/resolved/endpoint'])
                }
                return Mock(CloseableHttpResponse){
                    getEntity() >> new StringEntity(body, ContentType.APPLICATION_JSON)
                    getStatusLine() >> new BasicStatusLine(new ProtocolVersion('http',2,0),200, 'OK')
                }
            }
        }
        def globalScope = new GlobalScope(name, resourceRoot, castingDirector, eventBus)
        globalScope.remember('clientOfServiceUnderTest',new RemoteClientOfServiceUnderTest(httpMock,'http://localhost:8080/base'))
        globalScope.remember('runId', runId)
        globalScope.remember('outputResourceRoot', Paths.get('build','output'))
        globalScope.remember('journalRoot', Paths.get('build','journal'))
        globalScope.remember('baseUrlOfServiceUnderTest', 'http://service.com/under/test')
        globalScope.start()
        globalScope
    }
}
