package cucumber.wiremock

import com.github.ampie.wiremock.ScopedWireMockServer
import cucumber.scoping.GlobalScope
import cucumber.screenplay.Actor
import cucumber.screenplay.actors.OnStage

import java.nio.file.Paths

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom
import static cucumber.wiremock.RequestStrategies.a
import static cucumber.wiremock.RecordingStrategies.*
import static cucumber.screenplay.actors.OnStage.*

class WhenBuildingRecordingSpecifications extends WhenWorkingWithWireMock {

    def 'should associate a recording mapping with the current ActorInScope'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def tempDir = File.createTempDir("wiremock-screenplay-tests-WhenBuildingRecordingSpecifications", "")
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        shineSpotlightOn(johnSmith)
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(recordResponsesTo(tempDir.absolutePath))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.theActorInTheSpotlight().getScopePath())
        mappings.size() == 0
        def requestsToRecord = globalScope.enter(johnSmith).recall("requestsToRecordOrPlayback")
        requestsToRecord[0].calculateRecordingDirectory(globalScope).toString() == Paths.get(tempDir.absolutePath,"John_Smith").toString()
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.RECORD
        requestsToRecord[0].recordingSpecification.recordingDirectory == tempDir.absolutePath
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should associate the recording mapping with the all active actors when created under everybody'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def tempDir = File.createTempDir("wiremock-screenplay-tests-WhenBuildingRecordingSpecifications", "")
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)
        when:
        globalScope.shineSpotlightOn(actorNamed("John Smith"))
        globalScope.shineSpotlightOn(actorNamed("Jack_Smith"))
        forRequestsFrom(actorNamed("everybody")).allow(
                a(PUT).to("/home/path").to(recordResponsesTo(tempDir.absolutePath))
        )
        then:
        def mappings = globalScope.getEverybodyScope().recallImmediately("requestsToRecordOrPlayback")
        mappings.size() == 2
        mappings[0].recordingSpecification.journalModeOverride == JournalMode.RECORD
        mappings[0].recordingSpecification.recordingDirectory == tempDir.absolutePath
        mappings[0].userInScopeId == 'Jack_Smith'
        mappings[1].recordingSpecification.journalModeOverride == JournalMode.RECORD
        mappings[1].recordingSpecification.recordingDirectory == tempDir.absolutePath
        mappings[1].userInScopeId == 'John_Smith'
    }
    def 'should record responses to the current output resource directory when no path is specified'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)

        def johnSmith = actorNamed("John Smith")
        shineSpotlightOn(johnSmith)
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(recordResponses())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.theActorInTheSpotlight().getScopePath())
        mappings.size() == 0
        def requestsToRecord = globalScope.enter(johnSmith).recall("requestsToRecordOrPlayback")
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.RECORD
        requestsToRecord[0].calculateRecordingDirectory(globalScope).toString() == Paths.get("build","output","John_Smith").toString()
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should associate a playback recording mapping with the current ActorInScope'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def tempDir = File.createTempDir("wiremock-screenplay-tests-WhenBuildingRecordingSpecifications", "")
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)

        def johnSmith = actorNamed("John Smith")
        shineSpotlightOn(johnSmith)

        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(playbackResponsesFrom(tempDir.absolutePath))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.theActorInTheSpotlight().getScopePath())
        mappings.size() == 0
        def requestsToRecord = globalScope.enter(johnSmith).recall("requestsToRecordOrPlayback")
        requestsToRecord[0].calculateRecordingDirectory(globalScope).toString() == Paths.get(tempDir.absolutePath,"John_Smith").toString()
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.PLAYBACK
        requestsToRecord[0].recordingSpecification.recordingDirectory == tempDir.absolutePath
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should playback responses from the current input resource directory when no path is specified'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def nestedScope = globalScope.startFunctionalScope("nested1")

        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)

        def johnSmith = actorNamed("John Smith")
        shineSpotlightOn(johnSmith)
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(playbackResponses())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + theActorInTheSpotlight().getScopePath())
        mappings.size() == 0
        def requestsToRecord = nestedScope.enter(johnSmith).recall("requestsToRecordOrPlayback")
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.PLAYBACK
        requestsToRecord[0].calculateRecordingDirectory(nestedScope).toString() == Paths.get("src","test","resources","John_Smith", "nested1").toString()
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should map responses to the current journal directory when no path is specified'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def nestedScope = globalScope.startFunctionalScope("nested1")
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)

        def johnSmith = actorNamed("John Smith")
        shineSpotlightOn(johnSmith)
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/endpoint/path").to(mapToJournalDirectory())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + theActorInTheSpotlight().getScopePath())
        mappings.size() == 0
        def requestsToRecord = nestedScope.enter(johnSmith).recall("requestsToRecordOrPlayback")
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.enforceJournalModeInScope == true
        requestsToRecord[0].recordingSpecification.journalModeOverride == null
        requestsToRecord[0].calculateRecordingDirectory(nestedScope).toString() == Paths.get("build","journal","John_Smith","nested1").toString()
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should map responses to the current resource directory under the journal directory when no path is specified'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def nestedScope = globalScope.startFunctionalScope("nested1")
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        given:
        OnStage.present(globalScope)

        def johnSmith = actorNamed("John Smith")
        shineSpotlightOn(johnSmith)
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(mapToJournalDirectory("/tmp/journal"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.theActorInTheSpotlight().getScopePath())
        mappings.size() == 0
        def requestsToRecord = nestedScope.enter(johnSmith).recall("requestsToRecordOrPlayback")
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.enforceJournalModeInScope == true
        requestsToRecord[0].recordingSpecification.journalModeOverride == null
        requestsToRecord[0].calculateRecordingDirectory(nestedScope).toString() == Paths.get("/tmp","journal","nested1", "John_Smith").toString()
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }

}