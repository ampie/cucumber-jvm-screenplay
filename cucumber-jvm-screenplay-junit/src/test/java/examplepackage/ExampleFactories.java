package examplepackage;

import com.sbg.bdd.cucumber.screenplay.scoped.gsonpersona.GsonPersonaClient;
import com.sbg.bdd.cucumber.wiremock.annotations.ResourceRoots;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayUrls;
import com.sbg.bdd.cucumber.wiremock.memorizer.DefaultScreenplayFactories;
import com.sbg.bdd.screenplay.core.persona.PersonaClient;

public class ExampleFactories extends DefaultScreenplayFactories{
    @Override
    public PersonaClient createPersonaClient(ResourceRoots resourceRoots, ScreenplayUrls urls) {
        return new GsonPersonaClient();
    }
}
