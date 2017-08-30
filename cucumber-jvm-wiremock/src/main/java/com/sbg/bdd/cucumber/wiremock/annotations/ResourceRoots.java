package com.sbg.bdd.cucumber.wiremock.annotations;

import com.sbg.bdd.resource.ResourceContainer;

public interface ResourceRoots {
    ResourceContainer getFeatureFileRoot(ScreenplayUrls urls);
    ResourceContainer getPersonaRoot(ScreenplayUrls urls);
    ResourceContainer getInputRoot(ScreenplayUrls urls);
    ResourceContainer getOutputRoot(ScreenplayUrls urls);
    ResourceContainer getJournalRoot(ScreenplayUrls urls);
}
