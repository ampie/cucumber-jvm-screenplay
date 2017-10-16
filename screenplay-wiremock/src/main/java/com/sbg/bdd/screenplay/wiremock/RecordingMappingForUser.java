package com.sbg.bdd.screenplay.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.resource.file.DirectoryResourceRoot;
import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.actors.Performance;
import com.sbg.bdd.screenplay.core.util.NameConverter;
import com.sbg.bdd.wiremock.scoped.admin.model.JournalMode;
import com.sbg.bdd.wiremock.scoped.admin.model.ScopeLocalPriority;
import com.sbg.bdd.wiremock.scoped.client.ScopedWireMockClient;
import com.sbg.bdd.wiremock.scoped.client.builders.ExtendedMappingBuilder;
import com.sbg.bdd.wiremock.scoped.client.builders.ExtendedRequestPatternBuilder;
import com.sbg.bdd.wiremock.scoped.client.builders.RecordingSpecification;
import com.sbg.bdd.wiremock.scoped.integration.HeaderName;

import java.io.File;


//TODO move to server
public class RecordingMappingForUser {
    private ExtendedMappingBuilder mappingBuilder;
    private String userInScopeId;

    public RecordingMappingForUser(String userScopeName, ExtendedMappingBuilder mappingBuilder) {
        if (userScopeName.equals(Actor.EVERYBODY)) {
            throw new IllegalArgumentException();
        }
        this.userInScopeId = NameConverter.filesystemSafe(userScopeName);
        this.mappingBuilder = mappingBuilder;
    }

    public ExtendedMappingBuilder getMappingBuilder() {
        return mappingBuilder;
    }

    public String getUserInScopeId() {
        return userInScopeId;
    }

    public void saveRecordings(Scene scope) {
        ExtendedRequestPatternBuilder requestPatternBuilder = new ExtendedRequestPatternBuilder(this.mappingBuilder.getRequestPatternBuilder());
        ScopedWireMockClient wireMock = getWireMock(scope);
        wireMock.saveRecordingsForRequestPattern(deriveCorrelationPath(scope), requestPatternBuilder.build(), calculateRecordingDirectory(scope));
    }


    public ScopedWireMockClient getWireMock(Scene scope) {
        return scope.getPerformance().recall(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT);
    }

    public void loadRecordings(Scene scope) {
        ResourceContainer recordingDirectory = calculateRecordingDirectory(scope, userInScopeId);
        if (recordingDirectory != null) {
            //may not exist
            ScopedWireMockClient wireMock = getWireMock(scope);
            ExtendedRequestPatternBuilder requestPatternBuilder = new ExtendedRequestPatternBuilder(mappingBuilder.getRequestPatternBuilder());
            requestPatternBuilder.withHeader(HeaderName.ofTheCorrelationKey(), deriveCorrelationPath(scope));
            //TODO temp hack until we have moved this down to the server
            wireMock.serveRecordedMappingsAt(recordingDirectory, requestPatternBuilder.build(), (10 - scope.getLevel())*10 - priorityFor(scope).priority());
        }
    }

    private StringValuePattern deriveCorrelationPath(Scene scope) {
        return WireMock.equalTo(CorrelationPath.ofUserInScope(scope, userInScopeId));
    }

    public boolean enforceJournalModeInScope() {
        return getRecordingSpecification().enforceJournalModeInScope();
    }

    public JournalMode getJournalModeOverride() {
        return getRecordingSpecification().getJournalModeOverride();
    }


    private ScopeLocalPriority priorityFor(Scene scope) {
        return getRecordingSpecification().enforceJournalModeInScope() ? ScopeLocalPriority.JOURNAL : ScopeLocalPriority.RECORDINGS;
    }

    public ResourceContainer calculateRecordingDirectory(Scene scope) {
        return calculateRecordingDirectory(scope, this.userInScopeId);
    }

    private ResourceContainer calculateRecordingDirectory(Scene scope, String userScopeIdToUse) {
        if (getRecordingSpecification().enforceJournalModeInScope()) {
            //scoped based journalling is assumed to be an automated process where potentially huge amounts of exchanges are recorded and never checked it.
            //if we wanted to investigate what went wrong, we are more interested in the run scope than the persona
            //hence runscope1/runscope1.1/scenarioscope1.1.1/userInScopeId
            if (getRecordingSpecification().recordToCurrentResourceDir()) {
                //Record to journalRoot in scope
                return toFile(getResourceRoot(scope), scope.getSceneIdentifier(), userScopeIdToUse);
            } else if (!getResourceRoot(scope).fallsWithin(getRecordingSpecification().getRecordingDirectory())) {
                return toFile(getAbsoluteRecordingDir(), scope.getSceneIdentifier(), userScopeIdToUse);
            } else {
                return toFile(getResourceRoot(scope), getRecordingSpecification().getRecordingDirectory(), scope.getSceneIdentifier(), userScopeIdToUse);
            }
        } else {
            //explicit recording mapping is assumed to be a more manual process during development, fewer exchanges will
            //be recorded, possibly manually modified or converted to
            //templates, and then eventually be checked in
            //process where we are more interested in the persona associated with the exchanges
            //hence userScope_id/runscope1 / runscope1 .1 / scenarioscope1 .1 .1
            if (getRecordingSpecification().recordToCurrentResourceDir()) {
                return toFile(getResourceRoot(scope), userScopeIdToUse, scope.getSceneIdentifier());
            } else if (!getResourceRoot(scope).fallsWithin(getRecordingSpecification().getRecordingDirectory())) {
                //unlikely to be used this way
                return toFile(getAbsoluteRecordingDir(), userScopeIdToUse, scope.getSceneIdentifier());
            } else {
                //somewhere in the checked in persona dir, relative to the current resource dir
                return toFile(getResourceRoot(scope), userScopeIdToUse, scope.getSceneIdentifier(), getRecordingSpecification().getRecordingDirectory());
            }
        }
    }

    private DirectoryResourceRoot getAbsoluteRecordingDir() {
        return new DirectoryResourceRoot("absoluteDir", new File(getRecordingSpecification().getRecordingDirectory()));
    }


    private ResourceContainer getResourceRoot(Scene scope) {
        if (getRecordingSpecification().enforceJournalModeInScope()) {
            return scope.getPerformance().recall(WireMockScreenplayContext.JOURNAL_RESOURCE_ROOT);
        } else if (getRecordingSpecification().getJournalModeOverride() == JournalMode.RECORD) {
            return scope.getPerformance().recall(Performance.OUTPUT_RESOURCE_ROOT);
        } else {
            return scope.getPerformance().recall(Performance.INPUT_RESOURCE_ROOT);
        }
    }

    private RecordingSpecification getRecordingSpecification() {
        return this.mappingBuilder.getRecordingSpecification();
    }

    private ResourceContainer toFile(ResourceContainer root, String... trailingSegments) {
        return root.resolvePotentialContainer(trailingSegments);
    }

}
