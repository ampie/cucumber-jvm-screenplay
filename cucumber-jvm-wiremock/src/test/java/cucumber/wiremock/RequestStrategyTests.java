package cucumber.wiremock;

import cucumber.screenplay.Actor;
import cucumber.screenplay.DownstreamExpectation;
import org.hamcrest.Matcher;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static cucumber.screenplay.ScreenplayPhrases.actorNamed;
import static cucumber.screenplay.ScreenplayPhrases.forRequestsFrom;
import static cucumber.wiremock.CountMatcher.between;
import static cucumber.wiremock.CountMatcher.exactly;
import static cucumber.wiremock.RequestStrategies.a;
import static cucumber.wiremock.ResponseStrategies.returnTheFile;


public class RequestStrategyTests {


    public static DownstreamExpectation that(ExtendedMappingBuilder stub, Matcher<Integer> c) {
        return new DownstreamExpectation(stub, stub.getRequestPatternBuilder().wasMade(c));
    }


    @Test
    public void testit() throws Exception {
        Actor john = actorNamed("John");
        forRequestsFrom(john).expect(
                that(a(PUT).to("/asdf/adsf").will(returnTheFile("asdf")), exactly(4).times())
        );
        forRequestsFrom(john).allow(
                a(PUT).to("/asdf/adsf").to(returnTheFile("asdf"))
        );
        forRequestsFrom(john).verifyThat(a(PUT).to("/asdf/adsf").wasMade(between(3).and(5).times()));

    }




}
