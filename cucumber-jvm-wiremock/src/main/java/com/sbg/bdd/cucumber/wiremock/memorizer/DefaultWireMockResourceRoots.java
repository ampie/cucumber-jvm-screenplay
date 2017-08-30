package com.sbg.bdd.cucumber.wiremock.memorizer;

import com.sbg.bdd.cucumber.wiremock.annotations.ResourceRoots;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayUrls;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.actors.Performance;
import com.sbg.bdd.screenplay.wiremock.WireMockScreenplayContext;
import com.sbg.bdd.wiremock.scoped.WireMockResourceRoot;

public class DefaultWireMockResourceRoots implements ResourceRoots {
    private WireMockResourceRoot inputRoot;

    @Override
    public ResourceContainer getFeatureFileRoot(ScreenplayUrls urls) {
        return (ResourceContainer) getInputRoot(urls).getChild("features");
    }

    @Override
    public ResourceContainer getPersonaRoot(ScreenplayUrls urls) {
        return (ResourceContainer) getInputRoot(urls).getChild("personas");
    }

    @Override
    public ResourceContainer getInputRoot(ScreenplayUrls urls) {
        if (this.inputRoot == null) {
            this.inputRoot = new WireMockResourceRoot(urls.theWireMockBaseUrl(), Performance.INPUT_RESOURCE_ROOT);
        }
        return this.inputRoot;
    }

    @Override
    public ResourceContainer getOutputRoot(ScreenplayUrls urls) {
        return new WireMockResourceRoot(urls.theWireMockBaseUrl(), Performance.OUTPUT_RESOURCE_ROOT);
    }

    @Override
    public ResourceContainer getJournalRoot(ScreenplayUrls urls) {
        return new WireMockResourceRoot(urls.theWireMockBaseUrl(), WireMockScreenplayContext.JOURNAL_RESOURCE_ROOT);
    }
}
