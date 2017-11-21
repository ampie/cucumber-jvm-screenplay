package examplepackage;

import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayWireMockConfig;
import com.sbg.bdd.screenplay.cucumber.junit.CucumberWithWireMock;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(CucumberWithWireMock.class)
@ScreenplayWireMockConfig(sourceContext = "service",tags = {"not_me.*"},
        scenarioStatus = "disocvery", factories = ExampleFactories.class, urls = ExampleUrls.class, resourceRoots = ExampleResourceRoots.class)
@CucumberOptions()
public class ExampleExclusiveFilterTest {
}
