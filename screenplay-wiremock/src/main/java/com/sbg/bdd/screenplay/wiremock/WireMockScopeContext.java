package com.sbg.bdd.screenplay.wiremock;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.Resource;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.resource.ResourceFilter;
import com.sbg.bdd.resource.file.ReadableFileResource;
import com.sbg.bdd.resource.file.RootDirectoryResource;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.internal.BaseActorOnStage;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.screenplay.core.util.Optional;
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient;
import com.sbg.bdd.wiremock.scoped.recording.WireMockContext;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedMappingBuilder;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedRequestPatternBuilder;
import com.sbg.bdd.wiremock.scoped.recording.builders.JournalMode;
import com.sbg.bdd.wiremock.scoped.recording.endpointconfig.EndpointConfig;
import com.sbg.bdd.wiremock.scoped.recording.endpointconfig.EndpointConfigRegistry;

import java.io.File;
import java.util.*;

public class WireMockScopeContext implements WireMockContext {
    private static final int MAX_LEVELS = 10;
    private static final int PRIORITIES_PER_LEVEL = 10;
    private static final int EVERYBODY_PRIORITY_DECREMENT = PRIORITIES_PER_LEVEL / 2;

    public static Integer calculatePriorityFor(int scopeLevel, int localLevel) {
        return ((MAX_LEVELS - scopeLevel) * PRIORITIES_PER_LEVEL) + localLevel;
    }

    private final RecordingWireMockClient wireMock;
    private final ResourceContainer inputResourceRoot;
    private final ActorOnStage userInScope;
    private final EndpointConfigRegistry endpointConfigRegistry;
    private List<RecordingMappingForUser> requestsToRecordOrPlayback;
    
    public WireMockScopeContext(ActorOnStage userInScope) {
        this.wireMock = userInScope.recall("recordingWireMockClient");
        this.endpointConfigRegistry = userInScope.recall("endpointConfigRegistry");
        this.userInScope = userInScope;
        this.inputResourceRoot = this.userInScope.getScene().getPerformance().recall("inputResourceRoot");
        this.requestsToRecordOrPlayback = userInScope.recall("requestsToRecordOrPlayback");
        if (this.requestsToRecordOrPlayback == null) {
            this.requestsToRecordOrPlayback = new ArrayList<>();
            userInScope.remember("requestsToRecordOrPlayback", this.requestsToRecordOrPlayback);
        }
    }

    @Override
    public Set<EndpointConfig> allKnownExternalEndpoints() {
        return endpointConfigRegistry.allKnownExternalEndpoints();
    }

    @Override
    public EndpointConfig endpointUrlFor(String serviceEndpointPropertyName) {
        return endpointConfigRegistry.endpointUrlFor(serviceEndpointPropertyName);
    }

    @Override
    public ReadableResource resolveInputResource(String fileName) {
        if (!inputResourceRoot.fallsWithin(fileName)) {
            File file = new File(fileName);
            return new ReadableFileResource(new RootDirectoryResource(file.getParentFile()), file);
        } else {
            ResourceContainer personaRoot = (ResourceContainer) inputResourceRoot.resolveExisting(userInScope.getId());
            String scopePath = userInScope.getScene().getSceneIdentifier();
            if (scopePath.indexOf("/") <= 0) {
                return (ReadableResource) personaRoot.resolveExisting(fileName);
            } else {
                String resourcePath = scopePath.substring(scopePath.indexOf("/") + 1) + "/" + fileName;
                String[] relativeScopePath = resourcePath.split("\\/");
                return (ReadableResource) personaRoot.resolveExisting(relativeScopePath);
            }
        }
    }

    @Override
    public String getBaseUrlOfServiceUnderTest() {
        return userInScope.recall("baseUrlOfServiceUnderTest");
    }
    
    @Override
    public void register(ExtendedMappingBuilder child) {
        if (!shouldIgnoreMapping(child)) {
            child.getRequestPatternBuilder().ensureScopePath(correlationPattern());
            if (child.getResponseDefinitionBuilder() != null) {
                wireMock.register(child);
            }
            processRecordingSpecs(child, userInScope.getId());
        }
    }

    protected StringValuePattern correlationPattern() {
        Scene scope = userInScope.getScene();
        if (BaseActorOnStage.isEverybody(userInScope)) {
            return CorrelationPath.matching(scope, ".*");
        } else {
            return CorrelationPath.matching(scope, "/.*" + userInScope.getId());
        }
    }

    @Override
    public int count(ExtendedRequestPatternBuilder requestPatternBuilder) {
        requestPatternBuilder.ensureScopePath(correlationPattern());
        return wireMock.count(requestPatternBuilder);
    }

    @Override
    public Integer calculatePriority(int localLevel) {
        int scopeLevel = userInScope.getScene().getLevel();
        if (BaseActorOnStage.isEverybody(userInScope)) {
            //Allow everybody scope mappings to be overriden by PersonaScopes and GuestScope
            return ((MAX_LEVELS - scopeLevel) * PRIORITIES_PER_LEVEL) + localLevel + EVERYBODY_PRIORITY_DECREMENT;
        } else {
            return calculatePriorityFor(scopeLevel, localLevel);
        }
    }


    protected void processRecordingSpecs(ExtendedMappingBuilder builder, String personaIdToUse) {
        if (personaIdToUse.equals("everybody")) {
            for (String personaDir : allPersonaIds()) {
                processRecordingSpecs(builder, personaDir);
            }
        } else {
            if (builder.getRecordingSpecification().getJournalModeOverride() == JournalMode.RECORD) {
                requestsToRecordOrPlayback.add(new RecordingMappingForUser(personaIdToUse, builder));
            } else if (builder.getRecordingSpecification().getJournalModeOverride() == JournalMode.PLAYBACK) {
                RecordingMappingForUser recordingMappingForUser = new RecordingMappingForUser(personaIdToUse, builder);
                requestsToRecordOrPlayback.add(recordingMappingForUser);
                recordingMappingForUser.loadRecordings(userInScope.getScene());
            } else if (builder.getRecordingSpecification().enforceJournalModeInScope()) {
                RecordingMappingForUser recordingMappingForUser = new RecordingMappingForUser(personaIdToUse, builder);
                requestsToRecordOrPlayback.add(recordingMappingForUser);
                if (getJournalModeInScope() == JournalMode.PLAYBACK) {
                    recordingMappingForUser.loadRecordings(userInScope.getScene());
                }
            }
        }
    }

    private Set<String> allPersonaIds() {
        final PersonaClient personaClient = userInScope.getScene().getPerformance().recall("personaClient");
        List<Resource> list = Arrays.asList(inputResourceRoot.list(new ResourceFilter() {
            @Override
            public boolean accept(ResourceContainer dir, String name) {
                Resource file = dir.resolveExisting(name);
                if (file.getName().equals("everybody")) {
                    return false;
                } else if (file instanceof ResourceContainer) {
                    return file.getName().equals("guest") || ((ResourceContainer) file).resolveExisting(personaClient.getDefaultPersonaFileName()) != null;
                } else {
                    return false;
                }
            }

        }));
        TreeSet<String> result = new TreeSet<>();
        for (Resource o : list) {
            result.add(o.getName());
        }
        return result;
    }

    private JournalMode getJournalModeInScope() {
        return Optional.fromNullable(userInScope.recall(JournalMode.class)).or(JournalMode.NONE);
    }

    private boolean shouldIgnoreMapping(ExtendedMappingBuilder builder) {
        //In playback mode we ignore all builders, except those that enforce the journalModeInScope, which of course is playback
        return getJournalModeInScope() == JournalMode.PLAYBACK && !builder.getRecordingSpecification().enforceJournalModeInScope();
    }
}
