package cucumber.wiremock;

import com.github.ampie.wiremock.common.HeaderName;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import cucumber.scoping.VerificationScope;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static cucumber.scoping.IdGenerator.fromName;
import static cucumber.wiremock.WireMockContext.calculatePriorityFor;

public class RecordingMappingForUser  {
    private ExtendedMappingBuilder mappingBuilder;
    private String userScopeName;
    private String userScopeId;
    private VerificationScope executionScope;

    public RecordingMappingForUser(String userScopeName, ExtendedMappingBuilder mappingBuilder) {
        if (userScopeName.equals("everybody")) {
            throw new IllegalArgumentException();
        }
        this.userScopeName = userScopeName;
        this.userScopeId = fromName(userScopeName);
        this.mappingBuilder = mappingBuilder;
    }

    public ExtendedMappingBuilder getMappingBuilder() {
        return mappingBuilder;
    }

    public String getUserScopeName() {
        return userScopeName;
    }

    public String getUserScopeId() {
        return userScopeId;
    }

    public void saveRecordings(VerificationScope executionScope) {
        RequestPatternBuilder requestPatternBuilder = new ExtendedRequestPatternBuilder(this.mappingBuilder.getRequestPatternBuilder());
        RecordingWireMockClient wireMock = getWireMock(executionScope);
        wireMock.saveRecordingsForRequestPattern(equalTo(executionScope.getScopePath() + '/' + this.userScopeId), requestPatternBuilder.build(), calculateRecordingDirectory(executionScope));
    }

    public RecordingWireMockClient getWireMock(VerificationScope executionScope) {
        return executionScope.getGlobalScope().getEverybodyScope().recall(RecordingWireMockClient.class);
    }
    
    public void loadRecordings(VerificationScope executionScope) {
        File recordingDirectory = calculateRecordingDirectory(executionScope, userScopeId);
        if (recordingDirectory.exists()) {
            RecordingWireMockClient wireMock = getWireMock(executionScope);
            this.executionScope = executionScope;
            ExtendedRequestPatternBuilder requestPatternBuilder = new ExtendedRequestPatternBuilder(mappingBuilder.getRequestPatternBuilder());
            requestPatternBuilder.withHeader(HeaderName.ofTheCorrelationKey(), equalTo(this.executionScope.getScopePath() + '/' + userScopeId));
            wireMock.serveRecordedMappingsAt(recordingDirectory, requestPatternBuilder, priorityFor(executionScope));
        }
    }
    
    public boolean enforceJournalModeInScope() {
        return getRecordingSpecification().enforceJournalModeInScope();
    }
    
    public JournalMode getJournalModeOverride() {
        return getRecordingSpecification().getJournalModeOverride();
    }
    

    private int priorityFor(VerificationScope executionScope) {
        int localPriority = getRecordingSpecification().enforceJournalModeInScope() ? 1 : 2;
        int scopeLevel = executionScope.getLevel();
        return calculatePriorityFor(scopeLevel, localPriority);
    }

    private File calculateRecordingDirectory(VerificationScope executionScope) {
        return calculateRecordingDirectory(executionScope, this.userScopeId);
    }
    
    private File calculateRecordingDirectory(VerificationScope executionScope, String userScopeIdToUse) {
        if (getRecordingSpecification().enforceJournalModeInScope()) {
            //scoped based journalling is assumed to be an automated process where potentially huge amounts of exchanges are recorded and never checked it.
            //if we wanted to investigate what went wrong, we are more interested in the run scope than the persona
            //hence runscope1/runscope1.1/scenarioscope1.1.1/userScopeId
            //Also, journalling should never record to the currentResourcedir which would cause endless confusion whenActor loading templates, etc.
            if (new File(getRecordingSpecification().getRecordingDirectory()).isAbsolute()) {
                return toFile(getRecordingSpecification().getRecordingDirectory(), executionScope.getScopePath(), userScopeIdToUse);
            } else {
                return toFile(executionScope.getGlobalScope().getResourceRoot().toString(), getRecordingSpecification().getRecordingDirectory(), executionScope.getScopePath(), userScopeIdToUse);
            }
        } else {
            //explicit recording mapping is assumed to be a more manual process during development, fewer exchanges will
            //be recorded, possibly manually modified or converted to
            //templates, and then eventually be checked in
            //process where we are more interested in the persona associated with the exchanges
            //hence userScope_id/runscope1 / runscope1 .1 / scenarioscope1 .1 .1
            if (getRecordingSpecification().recordToCurrentResourceDir()) {
                return toFile(executionScope.getGlobalScope().getResourceRoot().toString(), userScopeIdToUse, executionScope.getScopePath());
            } else if (new File(getRecordingSpecification().getRecordingDirectory()).isAbsolute()) {
                //unlikely to be used this way
                return toFile(getRecordingSpecification().getRecordingDirectory(), userScopeIdToUse, executionScope.getScopePath());
            } else {
                //somewhere in the checked in persona dir, relative to the current resource dir
                return toFile(executionScope.getGlobalScope().getResourceRoot().toString(), userScopeIdToUse, executionScope.getScopePath(), getRecordingSpecification().getRecordingDirectory());
            }
        }
    }

    private RecordingSpecification getRecordingSpecification() {
        return this.mappingBuilder.getRecordingSpecification();
    }
    
    private File toFile(String... segment) {
        StringBuilder sb = new StringBuilder();
        for (String s : segment) {
            sb.append(s);
            sb.append(File.separatorChar);
        }
        return new File(sb.substring(0, sb.length() - 1));
    }
    
}
