package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.screenplay.wiremock.listeners.RecordingManagementListener
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.scoped.ScopingPhrases.everybody
import static com.sbg.bdd.screenplay.wiremock.RequestStrategies.a
import static com.sbg.bdd.wiremock.scoped.recording.strategies.RecordingStrategies.playbackResponsesFrom

class WhenPlayingBackResponses extends WhenWorkingWithWireMock{

    def 'should create a mapping for each non-header file in the resource directory'() throws Exception{
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def wireMockServer = initializeWireMock(globalScope)

        def recordingWireMockClient = new RecordingWireMockClient(wireMockServer)
        globalScope.everybodyScope.remember(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT, recordingWireMockClient)
        OnStage.present(globalScope)
        def outputPath = new File('src/test/resources').getAbsolutePath()
        forRequestsFrom(everybody()).allow(
                a(PUT).to('/home/path').to(playbackResponsesFrom(outputPath))
        )
        def nestedScope = globalScope.startFunctionalScope('nested1_recording_scope')
        def johnSmithOnStage = nestedScope.shineSpotlightOn(actorNamed('John Smith'))
        def jackSmithOnStage = nestedScope.shineSpotlightOn(actorNamed('Jack Smith'))
        def listener = new RecordingManagementListener();

        when:
        listener.loadRecordings(nestedScope)

        then:
        def jackMappings =wireMockServer.getMappingsInScope(CorrelationPath.of(jackSmithOnStage))
        jackMappings.size() == 2
        def johnMappings =wireMockServer.getMappingsInScope(CorrelationPath.of(johnSmithOnStage))
        johnMappings.size() == 2
    }
}
