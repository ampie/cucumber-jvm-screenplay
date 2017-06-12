package com.sbg.bdd.screenplay.wiremock

import com.sbg.bdd.screenplay.core.Actor
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.actors.Performance
import com.sbg.bdd.screenplay.scoped.GlobalScope
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import com.sbg.bdd.wiremock.scoped.recording.builders.JournalMode

import static OnStage.shineSpotlightOn
import static OnStage.theActorInTheSpotlight
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.forRequestsFrom
import static com.sbg.bdd.screenplay.wiremock.RequestStrategies.a
import static com.sbg.bdd.wiremock.scoped.recording.strategies.RecordingStrategies.*

class WhenBuildingRecordingSpecifications extends WhenWorkingWithWireMock {

    def 'should associate a recording mapping with the current ActorInScope wihtout registering on WireMock'() throws Exception {
        
        given:
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def tempDir = File.createTempDir("wiremock-screenplay-tests-WhenBuildingRecordingSpecifications", "")
        ScopedWireMockServer wireMockServer = initializeWireMock(globalScope)
        OnStage.present(globalScope)
        def johnSmith = actorNamed("John Smith")
        shineSpotlightOn(johnSmith)
        
        when:
        forRequestsFrom(johnSmith).allow(
                a(PUT).to("/home/path").to(recordResponsesTo(tempDir.absolutePath))
        )

        then:
        def mappings = wireMockServer.getMappingsInScope("localhost/"+wireMockServer.port()+"/5/" + globalScope.theActorInTheSpotlight().scopePath)
        mappings.size() == 0
        def requestsToRecord = globalScope.enter(johnSmith).recall(WireMockScreenplayContext.REQUESTS_TO_RECORD_OR_PLAYBACK)
        requestsToRecord[0].calculateRecordingDirectory(globalScope).getFile().toString() == new File(tempDir,"John_Smith").toString()
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.RECORD
        requestsToRecord[0].recordingSpecification.recordingDirectory == tempDir.absolutePath
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should associate the recording mapping with the all active actors when created under everybody'() throws Exception {

        given:
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def tempDir = File.createTempDir("wiremock-screenplay-tests-WhenBuildingRecordingSpecifications", "")
        initializeWireMock(globalScope)
        OnStage.present(globalScope)
        globalScope.shineSpotlightOn(actorNamed("John Smith"))
        globalScope.shineSpotlightOn(actorNamed("Jack_Smith"))

        when:
        forRequestsFrom(actorNamed(Actor.EVERYBODY)).allow(
                a(PUT).to("/home/path").to(recordResponsesTo(tempDir.absolutePath))
        )

        then:
        def mappings = globalScope.getEverybodyScope().recallImmediately(WireMockScreenplayContext.REQUESTS_TO_RECORD_OR_PLAYBACK)
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
        def mappings = wireMockServer.getMappingsInScope("localhost/"+wireMockServer.port()+"/5/" + globalScope.theActorInTheSpotlight().scopePath)
        mappings.size() == 0
        def requestsToRecord = globalScope.enter(johnSmith).recall(WireMockScreenplayContext.REQUESTS_TO_RECORD_OR_PLAYBACK)
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.RECORD
        requestsToRecord[0].calculateRecordingDirectory(globalScope).file.absolutePath == globalScope.recall(Performance.OUTPUT_RESOURCE_ROOT).resolvePotentialContainer("John_Smith").file.absolutePath
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should associate a playback recording mapping with the current ActorInScope'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def tempDir = File.createTempDir("wiremock-screenplay-tests-WhenBuildingRecordingSpecifications", "")
        
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
        def mappings = wireMockServer.getMappingsInScope("localhost/"+wireMockServer.port()+"/5/" + globalScope.theActorInTheSpotlight().scopePath)
        mappings.size() == 0
        def requestsToRecord = globalScope.enter(johnSmith).recall(WireMockScreenplayContext.REQUESTS_TO_RECORD_OR_PLAYBACK)
        requestsToRecord[0].calculateRecordingDirectory(globalScope).getFile().toString() == new File(tempDir.absolutePath,"John_Smith").toString()
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.PLAYBACK
        requestsToRecord[0].recordingSpecification.recordingDirectory == tempDir.absolutePath
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should playback responses from the current input resource directory when no path is specified'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def nestedScope = globalScope.startFunctionalScope("nested1")

        
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
        def mappings = wireMockServer.getMappingsInScope("localhost/"+wireMockServer.port()+"/5/" + theActorInTheSpotlight().scopePath)
        mappings.size() == 0
        def requestsToRecord = nestedScope.enter(johnSmith).recall(WireMockScreenplayContext.REQUESTS_TO_RECORD_OR_PLAYBACK)
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.journalModeOverride == JournalMode.PLAYBACK
        requestsToRecord[0].calculateRecordingDirectory(nestedScope).file.absolutePath == globalScope.recall(Performance.INPUT_RESOURCE_ROOT).resolvePotentialContainer('John_Smith', 'nested1').file.absolutePath
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should map responses to the current journal directory when no path is specified'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def nestedScope = globalScope.startFunctionalScope("nested1")
        
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
        def mappings = wireMockServer.getMappingsInScope("localhost/"+wireMockServer.port()+"/5/" + theActorInTheSpotlight().scopePath)
        mappings.size() == 0
        def requestsToRecord = nestedScope.enter(johnSmith).recall(WireMockScreenplayContext.REQUESTS_TO_RECORD_OR_PLAYBACK)
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.enforceJournalModeInScope == true
        requestsToRecord[0].recordingSpecification.journalModeOverride == null
        requestsToRecord[0].calculateRecordingDirectory(nestedScope).file.absolutePath == globalScope.recall(WireMockScreenplayContext.JOURNAL_RESOURCE_ROOT).resolvePotentialContainer("John_Smith","nested1").file.absolutePath
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }
    def 'should map responses to the current resource directory under the journal directory when no path is specified'() throws Exception {
        GlobalScope globalScope = buildGlobalScope('TestRun',5)
        def nestedScope = globalScope.startFunctionalScope("nested1")
        
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
        def mappings = wireMockServer.getMappingsInScope("localhost/"+wireMockServer.port()+"/5/" + globalScope.theActorInTheSpotlight().scopePath)
        mappings.size() == 0
        def requestsToRecord = nestedScope.enter(johnSmith).recall(WireMockScreenplayContext.REQUESTS_TO_RECORD_OR_PLAYBACK)
        requestsToRecord.size() == 1
        requestsToRecord[0].recordingSpecification.enforceJournalModeInScope == true
        requestsToRecord[0].recordingSpecification.journalModeOverride == null
        requestsToRecord[0].calculateRecordingDirectory(nestedScope).file.absolutePath == new File('/tmp/journal/nested1/John_Smith').absolutePath
        requestsToRecord[0].userInScopeId == 'John_Smith'
    }

}