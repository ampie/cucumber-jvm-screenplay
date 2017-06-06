package com.sbg.bdd.screenplay.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.resource.file.RootDirectoryResource;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.util.NameConverter;
import com.sbg.bdd.wiremock.scoped.integration.HeaderName;
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedMappingBuilder;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedRequestPatternBuilder;
import com.sbg.bdd.wiremock.scoped.recording.builders.JournalMode;
import com.sbg.bdd.wiremock.scoped.recording.builders.RecordingSpecification;
import com.sbg.bdd.wiremock.scoped.recording.endpointconfig.EndpointConfigRegistry;

import java.io.File;

import static com.sbg.bdd.screenplay.wiremock.WireMockScopeContext.calculatePriorityFor;


public class RecordingMappingForUser {
    private ExtendedMappingBuilder mappingBuilder;
    private String userInScopeId;

    public RecordingMappingForUser(String userScopeName, ExtendedMappingBuilder mappingBuilder) {
        if (userScopeName.equals("everybody")) {
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
        RecordingWireMockClient wireMock = getWireMock(scope);
        requestPatternBuilder.prepareForBuild((EndpointConfigRegistry) scope.recall("endpointConfigRegistry"));
        wireMock.saveRecordingsForRequestPattern(deriveCorrelationPath(scope), requestPatternBuilder.build(), calculateRecordingDirectory(scope));
    }


    public RecordingWireMockClient getWireMock(Scene scope) {
        return scope.getPerformance().recall("recordingWireMockClient");
    }
    
    public void loadRecordings(Scene scope) {
        ResourceContainer recordingDirectory = calculateRecordingDirectory(scope, userInScopeId);
        if(recordingDirectory!=null){
            //may not exist
            RecordingWireMockClient wireMock = getWireMock(scope);
            ExtendedRequestPatternBuilder requestPatternBuilder = new ExtendedRequestPatternBuilder(mappingBuilder.getRequestPatternBuilder());
            requestPatternBuilder.withHeader(HeaderName.ofTheCorrelationKey(), deriveCorrelationPath(scope));
            wireMock.serveRecordedMappingsAt(recordingDirectory, requestPatternBuilder, priorityFor(scope));
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
    

    private int priorityFor(Scene scope) {
        int localPriority = getRecordingSpecification().enforceJournalModeInScope() ? 1 : 2;
        int scopeLevel = scope.getLevel();
        return calculatePriorityFor(scopeLevel, localPriority);
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
                return toFile(getResourceRoot(scope), userScopeIdToUse, scope.getSceneIdentifier());
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

    private RootDirectoryResource getAbsoluteRecordingDir() {
        return new RootDirectoryResource(new File(getRecordingSpecification().getRecordingDirectory()));
    }


    private ResourceContainer getResourceRoot(Scene scope) {
        if (getRecordingSpecification().enforceJournalModeInScope()) {
            return scope.getPerformance().recall("journalRoot");
        } else if (getRecordingSpecification().getJournalModeOverride() == JournalMode.RECORD) {
            return scope.getPerformance().recall("outputResourceRoot");
        } else {
            return scope.getPerformance().recall("inputResourceRoot");
        }
    }

    private RecordingSpecification getRecordingSpecification() {
        return this.mappingBuilder.getRecordingSpecification();
    }
    
    private ResourceContainer toFile(ResourceContainer root, String... trailingSegments) {
        return root.resolvePotentialContainer(trailingSegments);
    }
    
}
