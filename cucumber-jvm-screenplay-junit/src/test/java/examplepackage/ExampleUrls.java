package examplepackage;

import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayUrls;

public class ExampleUrls implements ScreenplayUrls{
    @Override
    public String theServiceUnderTest() {
        return "http://someservce:8080";
    }

    @Override
    public String theWireMockBaseUrl() {
        return "http://wiremock:8080";
    }

    @Override
    public String thePersonaService() {
        return "http://persona:8080";
    }
}
