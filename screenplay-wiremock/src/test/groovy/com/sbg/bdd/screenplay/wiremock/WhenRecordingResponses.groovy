package com.sbg.bdd.screenplay.wiremock

import com.github.tomakehurst.wiremock.http.ContentTypeHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.util.Paths
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.screenplay.wiremock.listeners.RecordingManagementListener
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedExchange
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedRequest
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedResponse
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils

import java.io.File

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.scoped.ScopingPhrases.everybody
import static com.sbg.bdd.screenplay.wiremock.RequestStrategies.a
import static com.sbg.bdd.wiremock.scoped.recording.strategies.RecordingStrategies.recordResponsesTo

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
        globalScope.everybodyScope.remember('recordingWireMockClient', new RecordingWireMockClient(wireMockServer))
        OnStage.present(globalScope)
        def outputPath = Paths.get('build', 'output', 'recordings').getAbsoluteFile()
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
        def list = outputPath.listFiles()
        list[0].name == 'Jack_Smith'
        def operation1File = Paths.get(list[0].absoluteFile, 'nested1', 'service_GET_operation1_0.json')
        FileUtils.readFileToString(operation1File,'UTF-8') == "{\"name\"=\"value\"}"
        def operation1HeaderFile = Paths.get(list[0].absoluteFile, 'nested1', 'service_GET_operation1_0.headers.json')
        operation1HeaderFile.exists() == true
        list[1].name == 'John_Smith'
        def operation2File =Paths.get(list[1].absoluteFile, 'nested1', 'service_PUT_operation2_0.xml')
        FileUtils.readFileToString(operation2File,'UTF-8') == "<root/>"
    }


}
