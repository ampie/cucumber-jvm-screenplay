package com.sbg.bdd.cucumber.wiremock.memorizer;

import com.sbg.bdd.cucumber.wiremock.annotations.ResourceRoots;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayFactories;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayUrls;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayWireMockConfig;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.actors.Performance;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.screenplay.core.util.ScreenplayConfigurator;
import com.sbg.bdd.screenplay.wiremock.WireMockMemories;
import com.sbg.bdd.wiremock.scoped.admin.ScopedAdmin;
import com.sbg.bdd.wiremock.scoped.admin.model.JournalMode;
import com.sbg.bdd.wiremock.scoped.client.endpointconfig.EndpointConfigRegistry;

public class CucumberWireMockConfigurator implements ScreenplayConfigurator {
    private final JournalMode globalJournalMode;
    private final String scenarioStatus;
    private final String sourceContext;
    private ScreenplayUrls urls;
    private ResourceRoots resourceRoots;
    private ScreenplayFactories factories;
    private PersonaClient personaClient;

    public CucumberWireMockConfigurator(ScreenplayWireMockConfig annotation) {
        try {
            if (annotation.resourceRoots() == null || annotation.resourceRoots() == ResourceRoots.class) {
                throw new IllegalStateException("Please provide a ResourceRoots implementation by annotating your JUnit test class with '" + ScreenplayWireMockConfig.class.getName() + "'");
            }
            this.resourceRoots = annotation.resourceRoots().newInstance();
            if (annotation.urls() == null || annotation.urls() == ScreenplayUrls.class) {
                throw new IllegalStateException("Please provide a ScreenplayUrls implementation by annotating your JUnit test class with '" + ScreenplayWireMockConfig.class.getName() + "'");
            }
            this.urls = annotation.urls().newInstance();
            if (annotation.factories() == null || annotation.factories() == ScreenplayFactories.class) {
                throw new IllegalStateException("Please provide a ScreenplayFactories implementation by annotating your JUnit test class with '" + ScreenplayWireMockConfig.class.getName() + "'");
            }
            this.factories = annotation.factories().newInstance();
            this.globalJournalMode = annotation.globalJournalMode();
            this.scenarioStatus = annotation.scenarioStatus();
            this.sourceContext = annotation.sourceContext();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public void applyTo(Performance performance) {
        WireMockMemories remember = WireMockMemories.rememberFor(performance);
        ResourceContainer journalRoot = resourceRoots.getJournalRoot(urls);
        if (journalRoot != null) {
            remember.toUseTheJournalAt(journalRoot);
        }
        ResourceContainer inputRoot = resourceRoots.getInputRoot(urls);
        if (inputRoot != null) {
            remember.toReadResourcesFrom(inputRoot);
        }
        ResourceContainer outputRoot = resourceRoots.getOutputRoot(urls);
        if (outputRoot != null) {
            remember.toWriteResourcesTo(outputRoot);
        }
        EndpointConfigRegistry endpointConfigRegistry = factories.createEndpointConfigRegistry(resourceRoots, urls);
        if (endpointConfigRegistry != null) {
            remember.toPointTo(urls.theServiceUnderTest());
            //TODO this is ugly. Maybe split?
            remember.toUseTheEndpointConfigRegistry(endpointConfigRegistry);
        } else if (urls.theServiceUnderTest() != null) {
            remember.toPointTo(urls.theServiceUnderTest());
        }
        PersonaClient personaClient = getPersonaClient();
        if (personaClient != null) {
            remember.toUseThePersonaClient(personaClient);
        }
        ScopedAdmin scopedAdmin = factories.createWireMockAdmin(resourceRoots, urls);
        if (scopedAdmin != null) {
            remember.toUseWireMock(scopedAdmin);
        } else if (urls.theWireMockBaseUrl() != null) {
            remember.toUseWireMockAt(urls.theWireMockBaseUrl());
        }
        if (this.globalJournalMode != null) {
            remember.toUseTheJournalMode(globalJournalMode);
        }
    }

    @Override
    public String getName() {
        return sourceContext + "_" + scenarioStatus;
    }

    @Override
    public PersonaClient getPersonaClient() {
        if(personaClient == null){
            personaClient = factories.createPersonaClient(resourceRoots, urls);
        }
        return personaClient;
    }

    @Override
    public ResourceContainer getPersonaRoot() {
        return resourceRoots.getPersonaRoot(urls);
    }
    public ResourceContainer getFeatureFileRoot() {
        return resourceRoots.getFeatureFileRoot(urls);
    }

}
