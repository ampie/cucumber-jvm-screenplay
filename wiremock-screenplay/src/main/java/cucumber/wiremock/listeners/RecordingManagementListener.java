package cucumber.wiremock.listeners;


import cucumber.scoping.UserInScope;
import cucumber.scoping.UserTrackingScope;
import cucumber.scoping.annotations.ScopePhase;
import cucumber.scoping.annotations.SubscribeToScope;
import cucumber.screenplay.util.Optional;
import cucumber.wiremock.JournalMode;
import cucumber.wiremock.RecordingMappingForUser;

import java.util.ArrayList;
import java.util.List;

public class RecordingManagementListener {
    @SubscribeToScope(scopePhase = ScopePhase.BEFORE_COMPLETE)
    public void saveRecordings(UserTrackingScope scope) {
        for (RecordingMappingForUser m : getActiveRecordingOrPlaybackMappings(scope, JournalMode.RECORD)) {
            if (scope.getUsersInScope().containsKey(m.getUserInScopeId())) {
                m.saveRecordings(scope);
            }
        }
    }

    @SubscribeToScope(scopePhase = ScopePhase.AFTER_START)
    public void loadRecordings(UserTrackingScope scope) {
        for (RecordingMappingForUser m : getActiveRecordingOrPlaybackMappings(scope, JournalMode.PLAYBACK)) {
            m.loadRecordings(scope);
        }
    }

    public List<RecordingMappingForUser> getActiveRecordingOrPlaybackMappings(UserTrackingScope scope, JournalMode journalMode) {
        List<RecordingMappingForUser> activeRecordings = new ArrayList<>();
        if (scope.getContainingScope() != null) {
            activeRecordings.addAll(getActiveRecordingOrPlaybackMappings(scope.getContainingScope(), journalMode));
        }
        for (UserInScope userInScope : scope.getUsersInScope().values()) {
            activeRecordings.addAll(getRecordingOrPlaybackMappings(userInScope, journalMode));
        }
        return activeRecordings;
    }

    private List<RecordingMappingForUser> getRecordingOrPlaybackMappings(UserInScope userInScope, JournalMode journalMode) {
        List<RecordingMappingForUser> result = new ArrayList<>();
        List<RecordingMappingForUser> requestsToRecordOrPlayback = userInScope.recallImmediately("requestsToRecordOrPlayback");
        if (requestsToRecordOrPlayback != null) {
            for (RecordingMappingForUser r : requestsToRecordOrPlayback) {
                if(r.getJournalModeOverride() == journalMode || (r.enforceJournalModeInScope() && getJournalModeInScope(userInScope) == journalMode)){
                    result.add(r);
                }
            }
        }
        return result;
    }
    private JournalMode getJournalModeInScope(UserInScope userInScope) {
        return Optional.fromNullable(userInScope.recall(JournalMode.class)).or(JournalMode.NONE);
    }
}
