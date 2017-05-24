package cucumber.wiremock

import com.github.ampie.wiremock.ScopedWireMockServer
import com.github.ampie.wiremock.admin.CorrelationState
import cucumber.scoping.GlobalScope
import cucumber.screenplay.ActorOnStage
import cucumber.screenplay.DownstreamStub
import cucumber.screenplay.annotations.Step
import cucumber.screenplay.events.ScreenPlayEventBus
import cucumber.screenplay.internal.InstanceGetter

import cucumber.scoping.persona.local.LocalPersonaClient
import cucumber.screenplay.internal.BaseCastingDirector
import cucumber.screenplay.actors.OnStage
import cucumber.scoping.wiremock.listeners.ScopeManagementListener
import groovy.json.JsonOutput
import org.apache.http.ProtocolVersion
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine
import static cucumber.screenplay.ScreenplayPhrases.*
import static cucumber.screenplay.actors.OnStage.*

import java.nio.file.Paths

class WhenManagingScopeOnWireMock extends WhenWorkingWithWireMock {

    def 'should keep in sync'() throws Exception {
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun', ScopeManagementListener)
        OnStage.present(globalScope)

        when:
        def scope = globalScope.startFunctionalScope('nested1')
        def scenarioScope = scope.startScenario('scenario1')
        def innerStep=null
        forRequestsFrom(actorNamed('John Smith')).allow(new DownstreamStub() {
            @Override
            @Step('step1')
            void performOnStage(ActorOnStage actorOnStage) {
                innerStep=currentScene().recall('correlationState').currentStep
            }
        })
        def userScope = scope.shineSpotlightOn(actorNamed('John Smith'))

        then:
        scope.everybodyScope.recall('correlationState').correlationPath == '5/TestRun/nested1'
        userScope.recall('correlationState').correlationPath == '5/TestRun/nested1/John_Smith'
        innerStep == 'step1'
        scenarioScope.everybodyScope.recall('correlationState').currentStep == null
    }

    def buildGlobalScope(String name, Class<?>... glue) {
        def resourceRoot = Paths.get('src/test/resources')
        def eventBus = new ScreenPlayEventBus(Mock(InstanceGetter) {
            getInstance(_) >> { args -> args[0].newInstance() }
        })
        def castingDirector = new BaseCastingDirector(eventBus,new LocalPersonaClient(), resourceRoot)

        eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(glue)))
        def httpMock = Mock(CloseableHttpClient) {
            execute(_) >> { args ->
                HttpUriRequest request = args[0]
                def body = null;
                if (request.getURI().getPath().endsWith('/Property/all')) {
                    body = JsonOutput.toJson([properties: [
                            [propertyName: 'external.service.a', propertyValue: 'http://somehost.com/service/one/endpoint'],
                            [propertyName: 'external.service.b', propertyValue: 'http://somehost.com/service/two/endpoint']

                    ]])
                } else {
                    body = JsonOutput.toJson([propertyValue: 'http://somehost.com/resolved/endpoint'])
                }
                return Mock(CloseableHttpResponse) {
                    getEntity() >> new StringEntity(body, ContentType.APPLICATION_JSON)
                    getStatusLine() >> new BasicStatusLine(new ProtocolVersion('http', 2, 0), 200, 'OK')
                }
            }
        }
        def globalScope = new GlobalScope(name, resourceRoot, castingDirector, eventBus)
        globalScope.everybodyScope.remember(ClientOfServiceUnderTest.class.getName(), new RemoteClientOfServiceUnderTest(httpMock, 'http://localhost:8080/base'))
        globalScope.everybodyScope.remember('runId', 5)
        globalScope.everybodyScope.remember('outputResourceRoot', Paths.get('build', 'output'))
        globalScope.everybodyScope.remember('journalRoot', Paths.get('build', 'journal'))
        globalScope.everybodyScope.remember('baseUrlOfServiceUnderTest', 'http://service.com/under/test')
        globalScope.everybodyScope.remember('recordingWireMockClient', new RecordingWireMockClient(new ScopedWireMockServer()))

        globalScope.start()
        globalScope
    }

}
