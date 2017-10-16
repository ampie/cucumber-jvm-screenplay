package com.sbg.bdd.cucumber.wiremock.memorizer;

import com.sbg.bdd.cucumber.wiremock.annotations.ResourceRoots;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayFactories;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayUrls;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.wiremock.scoped.admin.ScopedAdmin;
import com.sbg.bdd.wiremock.scoped.client.ScopedHttpAdminClient;

import java.net.MalformedURLException;
import java.net.URL;

public class DefaultScreenplayFactories implements ScreenplayFactories {
    @Override
    public PersonaClient createPersonaClient(ResourceRoots resourceRoots, ScreenplayUrls urls) {
        return null;
    }

    @Override
    public ScopedAdmin createWireMockAdmin(ResourceRoots resourceRoots, ScreenplayUrls urls) {
        try {
            URL baseUrl = new URL(urls.theWireMockBaseUrl());
            return new ScopedHttpAdminClient(baseUrl.getHost(), baseUrl.getPort(), baseUrl.getPath());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
