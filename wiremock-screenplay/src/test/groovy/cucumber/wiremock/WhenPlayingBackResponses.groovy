package cucumber.wiremock

import cucumber.scoping.GlobalScope
import cucumber.screenplay.actors.OnStage
import cucumber.scoping.wiremock.CorrelationPath

import java.nio.file.Paths
import cucumber.scoping.wiremock.listeners.RecordingManagementListener

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static cucumber.scoping.ScopingPhrases.everybody
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom
import static cucumber.wiremock.RecordingStrategies.playbackResponsesFrom
import static cucumber.wiremock.RequestStrategies.a

class WhenPlayingBackResponses extends WhenWorkingWithWireMock{

    def 'should create a mapping for each non-header file in the resource directory'() throws Exception{
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun',5)

        def wireMockServer = initializeWireMock(globalScope)
        globalScope.everybodyScope.remember(new RecordingWireMockClient(wireMockServer))
        OnStage.present(globalScope)
        def outputPath = Paths.get('src', 'test', 'resources').toAbsolutePath()
        forRequestsFrom(everybody()).allow(
                a(PUT).to('/home/path').to(playbackResponsesFrom(outputPath.toString()))
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
