package com.sbg.bdd.cucumber.wiremock.annotations;

import com.sbg.bdd.resource.ResourceContainer;

public interface ResourceRoots {
    ResourceContainer getFeatureFileRoot();
    ResourceContainer getPersonaRoot();
    ResourceContainer getInputRoot();
    ResourceContainer getOutputRoot();
    ResourceContainer getJournalRoot();
}
