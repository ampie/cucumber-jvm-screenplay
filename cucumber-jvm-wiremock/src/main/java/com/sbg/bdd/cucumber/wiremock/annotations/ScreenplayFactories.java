package com.sbg.bdd.cucumber.wiremock.annotations;

import com.sbg.bdd.screenplay.core.persona.PersonaClient;
import com.sbg.bdd.wiremock.scoped.admin.ScopedAdmin;

public interface ScreenplayFactories {
    PersonaClient createPersonaClient(ResourceRoots resourceRoots, ScreenplayUrls urls);

    ScopedAdmin createWireMockAdmin(ResourceRoots resourceRoots, ScreenplayUrls urls);
}
