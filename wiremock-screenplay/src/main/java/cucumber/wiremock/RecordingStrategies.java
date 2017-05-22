package cucumber.wiremock;


public abstract class RecordingStrategies {
    public static ResponseStrategy mapToJournalDirectory(final String journalDirectoryOverride) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.getRecordingSpecification().mapsToJournalDirectory(journalDirectoryOverride);
                builder.getRequestPatternBuilder().changeUrlToPattern();
                builder.atPriority(scope.calculatePriority(1));
                return null;
            }
        };
    }

    public static ResponseStrategy mapToJournalDirectory() {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.getRecordingSpecification().mapsToJournalDirectory();
                builder.getRequestPatternBuilder().changeUrlToPattern();
                builder.atPriority(scope.calculatePriority(1));
                return null;
            }
        };
    }


    public static ResponseStrategy playbackResponsesFrom(final String recordingDirectory) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.getRequestPatternBuilder().changeUrlToPattern();
                builder.getRecordingSpecification().playbackResponsesFrom(recordingDirectory);
                builder.atPriority(scope.calculatePriority(2));
                return null;
            }
        };
    }

    public static ResponseStrategy playbackResponses() {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.getRequestPatternBuilder().changeUrlToPattern();
                builder.getRecordingSpecification().playbackResponses();
                builder.atPriority(scope.calculatePriority(2));
                return null;
            }
        };
    }

    public static ResponseStrategy recordResponsesTo(final String recordingDirectory) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.getRequestPatternBuilder().changeUrlToPattern();
                builder.getRecordingSpecification().recordingResponsesTo(recordingDirectory);
                builder.atPriority(scope.calculatePriority(2));
                return null;
            }
        };
    }

    public static ResponseStrategy recordResponses() {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.getRequestPatternBuilder().changeUrlToPattern();
                builder.getRecordingSpecification().recordingResponses();
                builder.atPriority(scope.calculatePriority(2));
                return null;
            }
        };
    }
}
