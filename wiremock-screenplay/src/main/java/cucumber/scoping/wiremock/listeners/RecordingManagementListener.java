package cucumber.scoping.wiremock.listeners;


import cucumber.screenplay.annotations.SceneEventType;
import cucumber.screenplay.annotations.SceneListener;
import cucumber.scoping.wiremock.RecordingMappingForUser;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.Scene;
import cucumber.screenplay.util.Optional;
import cucumber.wiremock.JournalMode;

import java.util.ArrayList;
import java.util.List;

public class RecordingManagementListener {
    @SceneListener(scopePhases = SceneEventType.BEFORE_COMPLETE)
    public void saveRecordings(Scene scene) {
        for (RecordingMappingForUser m : getActiveRecordingOrPlaybackMappings(scene, JournalMode.RECORD)) {
            if (scene.getActorsOnStage().containsKey(m.getUserInScopeId())) {
                m.saveRecordings(scene);
            }
        }
    }

    @SceneListener(scopePhases = SceneEventType.AFTER_START)
    public void loadRecordings(Scene scene) {
        for (RecordingMappingForUser m : getActiveRecordingOrPlaybackMappings(scene, JournalMode.PLAYBACK)) {
            m.loadRecordings(scene);
        }
    }

    public List<RecordingMappingForUser> getActiveRecordingOrPlaybackMappings(Scene scene, JournalMode journalMode) {
        List<RecordingMappingForUser> activeRecordings = new ArrayList<>();
        Scene parentScene = scene.recall("parentScene");
        if (parentScene != null) {
            activeRecordings.addAll(getActiveRecordingOrPlaybackMappings(parentScene, journalMode));
        }
        for (ActorOnStage userInScope : scene.getActorsOnStage().values()) {
            activeRecordings.addAll(getRecordingOrPlaybackMappings(userInScope, journalMode));
        }
        return activeRecordings;
    }

    private List<RecordingMappingForUser> getRecordingOrPlaybackMappings(ActorOnStage userInScope, JournalMode journalMode) {
        List<RecordingMappingForUser> result = new ArrayList<>();
        List<RecordingMappingForUser> requestsToRecordOrPlayback = userInScope.recallImmediately("requestsToRecordOrPlayback");
        if (requestsToRecordOrPlayback != null) {
            for (RecordingMappingForUser r : requestsToRecordOrPlayback) {
                if (r.getJournalModeOverride() == journalMode || (r.enforceJournalModeInScope() && getJournalModeInScope(userInScope) == journalMode)) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    private JournalMode getJournalModeInScope(ActorOnStage userInScope) {
        return Optional.fromNullable(userInScope.recall(JournalMode.class)).or(JournalMode.NONE);
    }
}
