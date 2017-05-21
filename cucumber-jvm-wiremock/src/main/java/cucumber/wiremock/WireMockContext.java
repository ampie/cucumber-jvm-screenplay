package cucumber.wiremock;

import com.github.ampie.wiremock.common.HeaderName;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import cucumber.scoping.EverybodyInScope;
import cucumber.scoping.UserInScope;
import cucumber.screenplay.util.Optional;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WireMockContext {
    private static final int MAX_LEVELS = 10;
    private static final int PRIORITIES_PER_LEVEL = 10;
    private static final int EVERYBODY_PRIORITY_DECREMENT = PRIORITIES_PER_LEVEL / 2;
    public static Integer calculatePriorityFor(int scopeLevel, int localLevel) {
        return ((MAX_LEVELS - scopeLevel) * PRIORITIES_PER_LEVEL) + localLevel;
    }

    private final RecordingWireMockClient wireMock;
    private final Integer runId;
    private final Path resourceRoot;
    private final UserInScope userInScope;
    private final ClientOfServiceUnderTest clientOfServiceUnderTest;
    private List<RecordingMappingForUser> requestsToRecordOrPlayback;
    
    public WireMockContext(UserInScope userInScope) {
        this.wireMock = userInScope.recall(RecordingWireMockClient.class);
        this.clientOfServiceUnderTest = userInScope.recall(ClientOfServiceUnderTest.class);
        this.runId = userInScope.recall("runId");
        this.userInScope = userInScope;
        this.resourceRoot = userInScope.getScope().getGlobalScope().getResourceRoot();
        this.requestsToRecordOrPlayback = userInScope.recall("requestsToRecordOrPlayback");
        if (this.requestsToRecordOrPlayback == null) {
            this.requestsToRecordOrPlayback = new ArrayList<RecordingMappingForUser>();
            userInScope.remember("requestsToRecordOrPlayback", this.requestsToRecordOrPlayback);
        }
    }

    public Map<String, String> allKnownExternalEndpoints() {
        return clientOfServiceUnderTest.allKnowExternalEndpoints();
    }

    public URL endpointUrlFor(String serviceEndpointPropertyName) {
        return clientOfServiceUnderTest.endpointUrlFor(serviceEndpointPropertyName);
    }

    public File resolveResource(String fileName) {
        return Paths.get(resourceRoot.toString(), userInScope.getScopePath().split("\\/")).toFile();
    }

    public String getBaseUrlOfServiceUnderTest() {
        return null;
    }
    
    public void register(WireMockRuleBuilder child) {
        if (!shouldIgnoreMapping(child)) {
            child.getRequestPatternBuilder().ensureScopePath(correlationPattern());
            if (child.getResponseDefinitionBuilder() != null) {
                wireMock.register(child);
            }
            processRecordingSpecs(child, userInScope.getId());
        }
    }

    protected StringValuePattern correlationPattern() {
        if(userInScope instanceof EverybodyInScope) {
            String pattern = userInScope.getScope().getScopePath() + ".*";
            return WireMock.matching(pattern);
        }else{
            String pattern = userInScope.getScope().getScopePath() + "/.*" + userInScope.getId();
            return WireMock.matching(pattern);
        }
    }

    public int count(ExtendedRequestPatternBuilder requestPatternBuilder) {
        requestPatternBuilder.ensureScopePath(correlationPattern());
        return wireMock.count(requestPatternBuilder);
    }

    public Integer calculatePriority(int localLevel) {
        if (userInScope instanceof EverybodyInScope) {
            //Allow everybody scope mappings to be overriden by PersonaScopes and GuestScope
            return ((MAX_LEVELS - userInScope.getScope().getLevel()) * PRIORITIES_PER_LEVEL) + localLevel + EVERYBODY_PRIORITY_DECREMENT;
        } else {
            int scopeLevel = userInScope.getScope().getLevel();
            return calculatePriorityFor(scopeLevel, localLevel);
        }
    }


    protected void processRecordingSpecs(ExtendedMappingBuilder builder, String personaIdToUse) {
        if (builder.getRecordingSpecification().getJournalModeOverride() == JournalMode.RECORD) {
            requestsToRecordOrPlayback.add(new RecordingMappingForUser(personaIdToUse, builder));
        } else if (builder.getRecordingSpecification().getJournalModeOverride() == JournalMode.PLAYBACK) {
            RecordingMappingForUser recordingMappingForUser = new RecordingMappingForUser(personaIdToUse, builder);
            requestsToRecordOrPlayback.add(recordingMappingForUser);
            recordingMappingForUser.loadRecordings(userInScope.getScope());
        } else if (builder.getRecordingSpecification().enforceJournalModeInScope()) {
            RecordingMappingForUser recordingMappingForUser = new RecordingMappingForUser(personaIdToUse, builder);
            requestsToRecordOrPlayback.add(recordingMappingForUser);
            if (getJournalModeInScope() == JournalMode.PLAYBACK) {
                recordingMappingForUser.loadRecordings(userInScope.getScope());
            }
        }
    }

    private JournalMode getJournalModeInScope() {
        return Optional.fromNullable(userInScope.recall(JournalMode.class)).or(JournalMode.NONE);
    }

    private boolean shouldIgnoreMapping(ExtendedMappingBuilder builder) {
        //In playback mode we ignore all builders, except those that enforce the journalModeInScope, which of course is playback
        return getJournalModeInScope() == JournalMode.PLAYBACK && !builder.getRecordingSpecification().enforceJournalModeInScope();
    }
}
