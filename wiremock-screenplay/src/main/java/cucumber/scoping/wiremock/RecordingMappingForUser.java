package cucumber.scoping.wiremock;

import com.github.ampie.wiremock.common.HeaderName;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import cucumber.screenplay.Scene;
import cucumber.screenplay.util.NameConverter;
import cucumber.wiremock.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static cucumber.scoping.wiremock.WireMockScopeContext.calculatePriorityFor;


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
        RequestPatternBuilder requestPatternBuilder = new ExtendedRequestPatternBuilder(this.mappingBuilder.getRequestPatternBuilder());
        RecordingWireMockClient wireMock = getWireMock(scope);
        wireMock.saveRecordingsForRequestPattern(deriveCorrelationPath(scope), requestPatternBuilder.build(), calculateRecordingDirectory(scope));
    }


    public RecordingWireMockClient getWireMock(Scene scope) {
        return scope.getPerformance().recall("recordingWireMockClient");
    }
    
    public void loadRecordings(Scene scope) {
        File recordingDirectory = calculateRecordingDirectory(scope, userInScopeId);
        if (recordingDirectory.exists()) {
            RecordingWireMockClient wireMock = getWireMock(scope);
            ExtendedRequestPatternBuilder requestPatternBuilder = new ExtendedRequestPatternBuilder(mappingBuilder.getRequestPatternBuilder());
            requestPatternBuilder.withHeader(HeaderName.ofTheCorrelationKey(), deriveCorrelationPath(scope));
            wireMock.serveRecordedMappingsAt(recordingDirectory, requestPatternBuilder, priorityFor(scope));
        }
    }

    private StringValuePattern deriveCorrelationPath(Scene scope) {
        return WireMock.equalTo(CorrelationPath.ofUserInScope(scope , userInScopeId));
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

    public File calculateRecordingDirectory(Scene scope) {
        return calculateRecordingDirectory(scope, this.userInScopeId);
    }
    
    private File calculateRecordingDirectory(Scene scope, String userScopeIdToUse) {
        if (getRecordingSpecification().enforceJournalModeInScope()) {
            //scoped based journalling is assumed to be an automated process where potentially huge amounts of exchanges are recorded and never checked it.
            //if we wanted to investigate what went wrong, we are more interested in the run scope than the persona
            //hence runscope1/runscope1.1/scenarioscope1.1.1/userInScopeId
            if (getRecordingSpecification().recordToCurrentResourceDir()) {
                //Record to journalRoot in scope
                return toFile(getJournalRoot(scope), userScopeIdToUse,  RelativeResourceDir.of(scope));
            } else if (new File(getRecordingSpecification().getRecordingDirectory()).isAbsolute()) {
                return toFile(getRecordingSpecification().getRecordingDirectory(), RelativeResourceDir.of(scope), userScopeIdToUse);
            } else {
                return toFile(getJournalRoot(scope), getRecordingSpecification().getRecordingDirectory(), RelativeResourceDir.of(scope), userScopeIdToUse);
            }
        } else {
            //explicit recording mapping is assumed to be a more manual process during development, fewer exchanges will
            //be recorded, possibly manually modified or converted to
            //templates, and then eventually be checked in
            //process where we are more interested in the persona associated with the exchanges
            //hence userScope_id/runscope1 / runscope1 .1 / scenarioscope1 .1 .1
            if (getRecordingSpecification().recordToCurrentResourceDir()) {
                return toFile(getResourceRoot(scope), userScopeIdToUse, RelativeResourceDir.of(scope));
            } else if (new File(getRecordingSpecification().getRecordingDirectory()).isAbsolute()) {
                //unlikely to be used this way
                return toFile(getRecordingSpecification().getRecordingDirectory(), userScopeIdToUse, RelativeResourceDir.of(scope));
            } else {
                //somewhere in the checked in persona dir, relative to the current resource dir
                return toFile(getResourceRoot(scope), userScopeIdToUse, RelativeResourceDir.of(scope), getRecordingSpecification().getRecordingDirectory());
            }
        }
    }

    private String getJournalRoot(Scene scope) {
        Path outputResourceRoot = scope.getPerformance().recall("journalRoot");
        return outputResourceRoot.toString();
    }


    private String getResourceRoot(Scene scope) {
        if (getRecordingSpecification().getJournalModeOverride() == JournalMode.RECORD) {
            Path outputResourceRoot = scope.getPerformance().recall("outputResourceRoot");
            return outputResourceRoot.toString();
        } else {
            return scope.getPerformance().getResourceRoot().toString();
        }
    }

    private RecordingSpecification getRecordingSpecification() {
        return this.mappingBuilder.getRecordingSpecification();
    }
    
    private File toFile(String firstSegment, String... trailingSegments) {
        return Paths.get(firstSegment, trailingSegments).toFile();
    }
    
}
