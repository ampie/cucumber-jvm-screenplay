package cucumber.wiremock

import com.github.ampie.wiremock.RecordedExchange
import com.github.ampie.wiremock.RecordedRequest
import com.github.ampie.wiremock.RecordedResponse
import com.github.ampie.wiremock.ScopedWireMockServer
import com.github.tomakehurst.wiremock.http.ContentTypeHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.RequestMethod
import cucumber.scoping.GlobalScope
import cucumber.screenplay.actors.OnStage
import org.apache.commons.io.FileUtils

import java.nio.file.Paths
import cucumber.wiremock.scoping.listeners.RecordingManagementListener

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static cucumber.scoping.ScopingPhrases.everybody
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom
import static cucumber.wiremock.RecordingStrategies.recordResponsesTo
import static cucumber.wiremock.RequestStrategies.a

class WhenRecordingResponses extends WhenWorkingWithWireMock{

    def 'should record both response body and response header files in the specified directory'() throws Exception{
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        globalScope.everybodyScope.remember('runId',5)
        def wireMockServer = Mock(ScopedWireMockServer){
            findMatchingExchanges(_) >> {
                def request1 = new RecordedRequest()
                request1.requestedUrl = 'http://somehost/context/service/operation1'
                request1.method=RequestMethod.GET
                def request2 = new RecordedRequest()
                request2.requestedUrl = 'http://somehost/context/service/operation2'
                request2.method=RequestMethod.PUT;
                def exchange1 = new RecordedExchange(request1, 'nested1', null)
                def response1 = new RecordedResponse()
                response1.status = 1
                response1.base64Body =new String(Base64.encoder.encode("{\"name\"=\"value\"}".bytes))
                response1.headers = new HttpHeaders().plus(new ContentTypeHeader('application/json'))
                exchange1.recordResponse(response1)
                def exchange2 = new RecordedExchange(request2, 'nested1', null)
                def response2 = new RecordedResponse()
                response2.headers = new HttpHeaders().plus(new ContentTypeHeader('application/xml'))
                response2.base64Body =new String(Base64.encoder.encode('<root/>'.bytes))
                exchange2.recordResponse(response2)
                return [exchange1, exchange2]
            }
        }
        globalScope.everybodyScope.remember(new RecordingWireMockClient(wireMockServer))
        OnStage.present(globalScope)
        def outputPath = Paths.get('build', 'output', 'recordings').toAbsolutePath()
        forRequestsFrom(everybody()).allow(
                a(PUT).to('/home/path').to(recordResponsesTo(outputPath.toString()))
        )
        def nestedScope = globalScope.startFunctionalScope('nested1')
        nestedScope.shineSpotlightOn(actorNamed('John Smith'))
        nestedScope.shineSpotlightOn(actorNamed('Jack Smith'))
        def listener = new RecordingManagementListener();

        when:
            listener.saveRecordings(nestedScope)

        then:
        def list = outputPath.toFile().listFiles()
        list[0].name == 'Jack_Smith'
        def operation1File = Paths.get(list[0].absolutePath, 'nested1', 'service_GET_operation1_0.json').toFile()
        FileUtils.readFileToString(operation1File,'UTF-8') == "{\"name\"=\"value\"}"
        def operation1HeaderFile = Paths.get(list[0].absolutePath, 'nested1', 'service_GET_operation1_0.headers.json').toFile()
        operation1HeaderFile.exists() == true
        list[1].name == 'John_Smith'
        def operation2File = Paths.get(list[1].absolutePath, 'nested1', 'service_PUT_operation2_0.xml').toFile()
        FileUtils.readFileToString(operation2File,'UTF-8') == "<root/>"
    }


}
