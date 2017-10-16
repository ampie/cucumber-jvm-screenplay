package com.sbg.bdd.screenplay.wiremock.listeners;


import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.Scene;
import com.sbg.bdd.screenplay.core.annotations.SceneEventType;
import com.sbg.bdd.screenplay.core.annotations.SceneListener;
import com.sbg.bdd.screenplay.core.util.Optional;
import com.sbg.bdd.screenplay.wiremock.RecordingMappingForUser;
import com.sbg.bdd.screenplay.wiremock.WireMockMemories;
import com.sbg.bdd.screenplay.wiremock.WireMockScreenplayContext;
import com.sbg.bdd.wiremock.scoped.admin.model.JournalMode;

import java.util.ArrayList;
import java.util.List;
//TODO move to server
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
        Scene parentScene = scene.recall(Scene.PARENT_SCENE);
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
        List<RecordingMappingForUser> requestsToRecordOrPlayback = userInScope.recallImmediately(WireMockScreenplayContext.REQUESTS_TO_RECORD_OR_PLAYBACK);
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
        return Optional.fromNullable(WireMockMemories.recallFrom(userInScope).theJournalModeToUse()).or(JournalMode.NONE);
    }
}
