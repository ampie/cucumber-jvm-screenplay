package com.sbg.bdd.screenplay.wiremock;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.Resource;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.resource.ResourceFilter;
import com.sbg.bdd.resource.file.DirectoryResourceRoot;
import com.sbg.bdd.resource.file.ReadableFileResource;
import com.sbg.bdd.screenplay.core.Actor;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.internal.BaseActorOnStage;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.screenplay.core.util.Optional;
import com.sbg.bdd.wiremock.scoped.admin.model.JournalMode;
import com.sbg.bdd.wiremock.scoped.client.ScopedWireMockClient;
import com.sbg.bdd.wiremock.scoped.client.WireMockContext;
import com.sbg.bdd.wiremock.scoped.client.builders.ExtendedMappingBuilder;
import com.sbg.bdd.wiremock.scoped.client.builders.ExtendedRequestPatternBuilder;

import java.io.File;
import java.net.URL;
import java.util.*;

public class WireMockScreenplayContext implements WireMockContext {
    public static final String PAYLOAD = "Payload";
    public static final String RECORDING_WIRE_MOCK_CLIENT = "ScopedWireMockClient";
    public static final String REQUESTS_TO_RECORD_OR_PLAYBACK = "requestsToRecordOrPlayback";
    public static final String BASE_URL_OF_SERVICE_UNDER_TEST = "baseUrlOfServiceUnderTest";
    public static final String PERSONA_CLIENT = "personaClient";
    public static final String PERSONA_RESOURCE_ROOT = "personaResourceRoot";
    public static final String JOURNAL_RESOURCE_ROOT = "journalResourceRoot";
    public static final String CORRELATION_STATE = "correlationState";
    public static final String PROXY_UNMAPPED_ENDPOINTS = "proxyUnmappedEndpoints";
    public static final String JOURNAL_MODE = "journalMode";
    public static final String WIRE_MOCK_ADMIN = "wireMockAdmin";
    public static final String WIRE_MOCK_PUBLIC_ADDRESS = "wireMockPublicAddress";
    public static final String INTEGRATION_SCOPE = "integrationScope";

    private final ScopedWireMockClient wireMock;
    private final ResourceContainer personaRoot;
    private final ActorOnStage userInScope;
    private List<RecordingMappingForUser> requestsToRecordOrPlayback;

    public WireMockScreenplayContext(ActorOnStage userInScope) {
        this.wireMock = userInScope.recall(RECORDING_WIRE_MOCK_CLIENT);
        this.userInScope = userInScope;
        this.personaRoot = this.userInScope.getScene().getPerformance().recall(PERSONA_RESOURCE_ROOT);
        this.requestsToRecordOrPlayback = userInScope.recall(REQUESTS_TO_RECORD_OR_PLAYBACK);
        if (this.requestsToRecordOrPlayback == null) {
            this.requestsToRecordOrPlayback = new ArrayList<>();
            userInScope.remember(REQUESTS_TO_RECORD_OR_PLAYBACK, this.requestsToRecordOrPlayback);
        }
    }

    @Override
    public String getCorrelationPath() {
        Scene scope = userInScope.getScene();
        if (BaseActorOnStage.isEverybody(userInScope)) {
            return CorrelationPath.of(scope);
        } else {
            return CorrelationPath.of(userInScope);
        }
    }

    @Override
    public ReadableResource resolveInputResource(String fileName) {
        if (!personaRoot.fallsWithin(fileName)) {
            File file = new File(fileName);
            return new ReadableFileResource(new DirectoryResourceRoot("inputRoot", file.getParentFile()), file);
        } else {
            ResourceContainer personaRoot = (ResourceContainer) this.personaRoot.resolveExisting(userInScope.getId());
            String scopePath = userInScope.getScene().getSceneIdentifier();
            String resourcePath = scopePath + "/" + fileName;
            String[] relativeScopePath = resourcePath.split("\\/");
            return (ReadableResource) personaRoot.resolveOrFail(relativeScopePath);
        }
    }

    @Override
    public String getBaseUrlOfServiceUnderTest() {
        URL url =userInScope.recall(BASE_URL_OF_SERVICE_UNDER_TEST);
        return url==null?null:url.toExternalForm();
    }

    @Override
    public void register(ExtendedMappingBuilder builder) {
        if (!shouldIgnoreMapping(builder)) {
            builder.getRequestPatternBuilder().ensureScopePath(correlationPattern());
            if (builder.getResponseDefinitionBuilder() != null) {
                wireMock.register(builder.build());
            }
            processRecordingSpecs(builder, userInScope.getId());
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
        requestPatternBuilder.setCorrelationPath(getCorrelationPath());
        return wireMock.count(requestPatternBuilder.build());
    }

    //TODO move to server
    protected void processRecordingSpecs(ExtendedMappingBuilder builder, String personaIdToUse) {
        if (personaIdToUse.equals(Actor.EVERYBODY)) {
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
    //TODO Move to server???
    private Set<String> allPersonaIds() {
        final PersonaClient personaClient = userInScope.getScene().getPerformance().recall(PERSONA_CLIENT);
        List<Resource> list = Arrays.asList(personaRoot.list(new ResourceFilter() {
            @Override
            public boolean accept(ResourceContainer dir, String name) {
                Resource file = dir.resolveExisting(name);
                if (file.getName().equals(Actor.EVERYBODY)) {
                    return false;
                } else if (file instanceof ResourceContainer) {
                    return file.getName().equals(Actor.GUEST) || ((ResourceContainer) file).resolveExisting(personaClient.getDefaultPersonaFileName()) != null;
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
