package com.sbg.bdd.screenplay.core.util;

import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.actors.Performance;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;

public interface ScreenplayConfigurator {

    void applyTo(Performance performance);

    String getName();

    PersonaClient getPersonaClient();

    ResourceContainer getPersonaRoot();
}
