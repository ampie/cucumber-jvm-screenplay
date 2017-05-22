package cucumber.wiremock

import com.github.ampie.wiremock.ScopedWireMockServer
import com.github.tomakehurst.wiremock.common.Json
import cucumber.scoping.FunctionalScope
import cucumber.scoping.GlobalScope
import cucumber.screenplay.actors.OnStage
import groovy.json.JsonSlurper

import java.nio.file.Paths

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static cucumber.screenplay.ScreenplayPhrases.actorNamed
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom
import static cucumber.wiremock.RequestStrategies.a
import static cucumber.wiremock.RecordingStrategies.*

class WhenBuildingRecordingSpecifications extends WhenWorkingWithWireMock {

    def 'should associate a recording mapping with the current ActorInScope'() throws Exception {
        GlobalScope globalScope = buildGlobalScope("TestRun")
        def tempDir = File.createTempDir("wiremock-screenplay-tests-WhenBuildingRecordingSpecifications", "")
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        forRequestsFrom(actorNamed("John Smith")).allow(
                a(PUT).to("/home/path").to(recordResponsesTo(tempDir.absolutePath))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.getCurrentUserInScope().getScopePath())
        mappings.size() == 0
        def requestsToRecord = globalScope.getCurrentUserInScope().recall("requestsToRecordOrPlayback")
        requestsToRecord[0].calculateRecordingDirectory(globalScope).toString() == Paths.get(tempDir.absolutePath,"John_Smith").toString()
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.RECORD
        requestsToRecord[0].recordingSpecification.recordingDirectory == tempDir.absolutePath
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should associate the recording mapping with the all active actors when created under everybody'() throws Exception {
        GlobalScope globalScope = buildGlobalScope("TestRun")
        def tempDir = File.createTempDir("wiremock-screenplay-tests-WhenBuildingRecordingSpecifications", "")
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        globalScope.enter(actorNamed("John Smith"))
        globalScope.enter(actorNamed("Jack_Smith"))
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
        GlobalScope globalScope = buildGlobalScope("TestRun")
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        forRequestsFrom(actorNamed("John Smith")).allow(
                a(PUT).to("/home/path").to(recordResponses())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.getCurrentUserInScope().getScopePath())
        mappings.size() == 0
        def requestsToRecord = globalScope.getCurrentUserInScope().recall("requestsToRecordOrPlayback")
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.RECORD
        requestsToRecord[0].calculateRecordingDirectory(globalScope).toString() == Paths.get("build","output","John_Smith").toString()
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should associate a playback recording mapping with the current ActorInScope'() throws Exception {
        GlobalScope globalScope = buildGlobalScope("TestRun")
        def tempDir = File.createTempDir("wiremock-screenplay-tests-WhenBuildingRecordingSpecifications", "")
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        forRequestsFrom(actorNamed("John Smith")).allow(
                a(PUT).to("/home/path").to(playbackResponsesFrom(tempDir.absolutePath))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.getCurrentUserInScope().getScopePath())
        mappings.size() == 0
        def requestsToRecord = globalScope.getCurrentUserInScope().recall("requestsToRecordOrPlayback")
        requestsToRecord[0].calculateRecordingDirectory(globalScope).toString() == Paths.get(tempDir.absolutePath,"John_Smith").toString()
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.PLAYBACK
        requestsToRecord[0].recordingSpecification.recordingDirectory == tempDir.absolutePath
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should playback responses from the current input resource directory when no path is specified'() throws Exception {
        GlobalScope globalScope = buildGlobalScope("TestRun")
        def nestedScope = globalScope.startFunctionalScope("nested1")

        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        forRequestsFrom(actorNamed("John Smith")).allow(
                a(PUT).to("/home/path").to(playbackResponses())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.getCurrentUserInScope().getScopePath())
        mappings.size() == 0
        def requestsToRecord = nestedScope.getCurrentUserInScope().recall("requestsToRecordOrPlayback")
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.PLAYBACK
        requestsToRecord[0].calculateRecordingDirectory(nestedScope).toString() == Paths.get("src","test","resources","John_Smith", "nested1").toString()
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should map responses to the current journal directory when no path is specified'() throws Exception {
        GlobalScope globalScope = buildGlobalScope("TestRun")
        def nestedScope = globalScope.startFunctionalScope("nested1")
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        forRequestsFrom(actorNamed("John Smith")).allow(
                a(PUT).to("/endpoint/path").to(mapToJournalDirectory())
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.getCurrentUserInScope().getScopePath())
        mappings.size() == 0
        def requestsToRecord = nestedScope.getCurrentUserInScope().recall("requestsToRecordOrPlayback")
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.enforceJournalModeInScope == true
        requestsToRecord[0].recordingSpecification.journalModeOverride == null
        requestsToRecord[0].calculateRecordingDirectory(nestedScope).toString() == Paths.get("build","journal","John_Smith","nested1").toString()
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should map responses to the current resource directory under the journal directory when no path is specified'() throws Exception {
        GlobalScope globalScope = buildGlobalScope("TestRun")
        def nestedScope = globalScope.startFunctionalScope("nested1")
        def i = 5
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope, i)
        given:
        OnStage.present(globalScope)
        when:
        forRequestsFrom(actorNamed("John Smith")).allow(
                a(PUT).to("/home/path").to(mapToJournalDirectory("/tmp/journal"))
        )
        then:
        def mappings = wireMockServer.getMappingsInScope("5/" + globalScope.getCurrentUserInScope().getScopePath())
        mappings.size() == 0
        def requestsToRecord = nestedScope.getCurrentUserInScope().recall("requestsToRecordOrPlayback")
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.enforceJournalModeInScope == true
        requestsToRecord[0].recordingSpecification.journalModeOverride == null
        requestsToRecord[0].calculateRecordingDirectory(nestedScope).toString() == Paths.get("/tmp","journal","nested1", "John_Smith").toString()
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }

}