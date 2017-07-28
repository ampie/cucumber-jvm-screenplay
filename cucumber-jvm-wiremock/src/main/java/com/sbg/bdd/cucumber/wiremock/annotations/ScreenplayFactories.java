package com.sbg.bdd.cucumber.wiremock.annotations;

import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.wiremock.scoped.client.ScopedWireMockClient;
import com.sbg.bdd.wiremock.scoped.client.endpointconfig.EndpointConfigRegistry;

public interface ScreenplayFactories {
    PersonaClient createPersonaClient(ResourceRoots resourceRoots, ScreenplayUrls urls);
    ScopedWireMockClient createWireMockClient(ResourceRoots resourceRoots, ScreenplayUrls urls);
    EndpointConfigRegistry createEndpointConfigRegistry(ResourceRoots resourceRoots, ScreenplayUrls urls);
}
