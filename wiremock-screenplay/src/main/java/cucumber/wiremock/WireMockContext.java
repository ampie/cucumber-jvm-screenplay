package cucumber.wiremock;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import cucumber.scoping.EverybodyInScope;
import cucumber.scoping.UserInScope;
import cucumber.scoping.UserTrackingScope;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.util.Optional;
import cucumber.wiremock.scoping.CorrelationPath;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class WireMockContext implements EndpointPropertyResolver {
    private static final int MAX_LEVELS = 10;
    private static final int PRIORITIES_PER_LEVEL = 10;
    private static final int EVERYBODY_PRIORITY_DECREMENT = PRIORITIES_PER_LEVEL / 2;

    public static Integer calculatePriorityFor(int scopeLevel, int localLevel) {
        return ((MAX_LEVELS - scopeLevel) * PRIORITIES_PER_LEVEL) + localLevel;
    }

    private final RecordingWireMockClient wireMock;
    private final Path resourceRoot;
    private final UserInScope userInScope;
    private final ClientOfServiceUnderTest clientOfServiceUnderTest;
    private List<RecordingMappingForUser> requestsToRecordOrPlayback;
    
    public WireMockContext(ActorOnStage userInScope) {
        this.wireMock = userInScope.recall(RecordingWireMockClient.class);
        this.clientOfServiceUnderTest = userInScope.recall(ClientOfServiceUnderTest.class);
        this.userInScope = (UserInScope) userInScope;
        this.resourceRoot = this.userInScope.getScope().getGlobalScope().getResourceRoot();
        this.requestsToRecordOrPlayback = userInScope.recall("requestsToRecordOrPlayback");
        if (this.requestsToRecordOrPlayback == null) {
            this.requestsToRecordOrPlayback = new ArrayList<>();
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
        Path personaRoot = Paths.get(resourceRoot.toString(), userInScope.getId());
        String scopePath = userInScope.getScope().getScopePath();
        if (scopePath.indexOf("/") <= 0) {
            return personaRoot.resolve(fileName).toFile();
        } else {
            String[] relativeScopePath = scopePath.substring(scopePath.indexOf("/") + 1).split("\\/");
            return Paths.get(personaRoot.toString(), relativeScopePath).resolve(fileName).toFile();
        }
    }

    public String getBaseUrlOfServiceUnderTest() {
        return userInScope.recall("baseUrlOfServiceUnderTest");
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
        UserTrackingScope scope = userInScope.getScope();
        if (userInScope instanceof EverybodyInScope) {
            return CorrelationPath.matching(scope, ".*");
        } else {
            return CorrelationPath.matching(scope, "/.*" + userInScope.getId());
        }
    }

    public int count(ExtendedRequestPatternBuilder requestPatternBuilder) {
        requestPatternBuilder.ensureScopePath(correlationPattern());
        return wireMock.count(requestPatternBuilder);
    }

    public Integer calculatePriority(int localLevel) {
        int scopeLevel = userInScope.getScope().getLevel();
        if (userInScope instanceof EverybodyInScope) {
            //Allow everybody scope mappings to be overriden by PersonaScopes and GuestScope
            return ((MAX_LEVELS - scopeLevel) * PRIORITIES_PER_LEVEL) + localLevel + EVERYBODY_PRIORITY_DECREMENT;
        } else {
            return calculatePriorityFor(scopeLevel, localLevel);
        }
    }


    protected void processRecordingSpecs(ExtendedMappingBuilder builder, String personaIdToUse) {
        if (personaIdToUse.equals("everybody")) {
            for (String personaId : allPersonaIds()) {
                processRecordingSpecs(builder, personaId);
            }
        } else {
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
    }

    private Collection<String> allPersonaIds() {
        File resourceRootDir = userInScope.getScope().getGlobalScope().getResourceRoot().toFile();
        return new TreeSet<>(Arrays.asList(resourceRootDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File file = new File(dir,name);
                return !file.getName().equals("everybody") && file.isDirectory() && (new File(file, "persona.json").exists() || file.getName().equals("guest"));
            }
        })));
    }

    private JournalMode getJournalModeInScope() {
        return Optional.fromNullable(userInScope.recall(JournalMode.class)).or(JournalMode.NONE);
    }

    private boolean shouldIgnoreMapping(ExtendedMappingBuilder builder) {
        //In playback mode we ignore all builders, except those that enforce the journalModeInScope, which of course is playback
        return getJournalModeInScope() == JournalMode.PLAYBACK && !builder.getRecordingSpecification().enforceJournalModeInScope();
    }
}
