package cucumber.wiremock;


public class RecordingSpecification {
    private boolean recordToCurrentResourceDir = false;
    private boolean enforceJournalModeInScope = false;
    private JournalMode journalModeOverride;
    private String recordingDirectory;
    public  RecordingSpecification(){
        
    }
    public RecordingSpecification(RecordingSpecification source) {
        this.journalModeOverride = source.journalModeOverride;
        this.recordingDirectory = source.recordingDirectory;
        this.enforceJournalModeInScope = source.enforceJournalModeInScope;
        this.recordToCurrentResourceDir = source.recordToCurrentResourceDir;

    }

    public boolean recordToCurrentResourceDir() {
        return recordToCurrentResourceDir;
    }

    public boolean enforceJournalModeInScope() {
        return enforceJournalModeInScope;
    }

    public RecordingSpecification recordingResponses() {
        recordToCurrentResourceDir = true;
        journalModeOverride = JournalMode.RECORD;
        return this;
    }

    public RecordingSpecification recordingResponsesTo(String directory) {
        this.recordingDirectory = directory;
        recordToCurrentResourceDir = false;
        journalModeOverride = JournalMode.RECORD;
        return this;
    }

    public RecordingSpecification playbackResponses() {
        recordToCurrentResourceDir = true;
        journalModeOverride = JournalMode.PLAYBACK;
        return this;
    }

    public RecordingSpecification playbackResponsesFrom(String directory) {
        this.recordingDirectory = directory;
        recordToCurrentResourceDir = false;
        journalModeOverride = JournalMode.PLAYBACK;
        return this;
    }

    public RecordingSpecification mapsToJournalDirectory(String journalDirectory) {
        this.recordingDirectory = journalDirectory;
        recordToCurrentResourceDir = false;
        enforceJournalModeInScope = true;
        return this;
    }


    public JournalMode getJournalModeOverride() {
        return journalModeOverride;
    }

    public String getRecordingDirectory() {
        return recordingDirectory;
    }
}
