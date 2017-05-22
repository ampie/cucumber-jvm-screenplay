package cucumber.wiremock

import com.github.ampie.wiremock.ScopedWireMockServer
import cucumber.scoping.GlobalScope
import cucumber.scoping.events.InstanceGetter
import cucumber.scoping.events.ScopeEventBus
import cucumber.scoping.persona.local.LocalPersonaClient
import cucumber.scoping.screenplay.ScopedCastingDirector
import spock.lang.Specification

import java.nio.file.Paths

abstract class WhenWorkingWithWireMock extends Specification{
    public static final int MAX_LEVELS = 10;
    public static final int PRIORITIES_PER_LEVEL = 10;
    static final int EVERYBODY_PRIORITY_DECREMENT = PRIORITIES_PER_LEVEL / 2;
    def initializeWireMock(GlobalScope globalScope, int runId) {
        def wireMockServer = new ScopedWireMockServer()
        globalScope.remember(new RecordingWireMockClient(wireMockServer))
        globalScope.remember("runId", runId)
        wireMockServer
    }

    def buildGlobalScope(String name) {
        def resourceRoot = Paths.get("src/test/resources")

        def castingDirector = new ScopedCastingDirector(new LocalPersonaClient(), resourceRoot)

        def eventBus = new ScopeEventBus(new InstanceGetter() {
            @Override
            def <T> T getInstance(Class<T> type) {
                return type.newInstance();
            }
        }, 2)

        def globalScope = new GlobalScope(name, resourceRoot, castingDirector, eventBus)
        globalScope.remember(ClientOfServiceUnderTest.class.getName(), new ClientOfServiceUnderTest(){

            @Override
            URL endpointUrlFor(String serviceEndpointPropertyName) {
                return new URL("http://somehost.com/resolved/endpoint")
            }

            @Override
            Map<String, String> allKnowExternalEndpoints() {
                def result = new TreeMap<String,String>()
                result.put("external.service.a", "http://somehost.com/service/one/endpoint")
                result.put("external.service.b", "http://somehost.com/service/two/endpoint")
                return result
            }
        })
        globalScope.remember("outputResourceRoot", Paths.get("build","output"))
        globalScope.remember("journalRoot", Paths.get("build","journal"))
        globalScope.remember("baseUrlOfServiceUnderTest", "http://service.com/under/test")
        globalScope
    }
}
