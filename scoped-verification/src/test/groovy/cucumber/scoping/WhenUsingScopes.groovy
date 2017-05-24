package cucumber.scoping

import cucumber.scoping.events.ScreenplayLifecycleSync
import cucumber.screenplay.events.ScreenPlayEventBus
import cucumber.screenplay.internal.BaseCastingDirector
import cucumber.screenplay.internal.InstanceGetter
import cucumber.screenplay.persona.properties.PropertiesPersonaClient
import spock.lang.Specification

import java.nio.file.Paths

abstract class WhenUsingScopes extends Specification {

    def buildGlobalScope(String name, Class<?>... glue) {
        def resourceRoot = Paths.get('src/test/resources')
        def eventBus = new ScreenPlayEventBus(Mock(InstanceGetter) {
            getInstance(_) >> { args -> args[0].newInstance() }
        })

        def castingDirector = new BaseCastingDirector(eventBus, new PropertiesPersonaClient(), resourceRoot)
        def classes = new HashSet<Class<?>>(Arrays.asList(glue))
        classes.add(ScreenplayLifecycleSync)
        eventBus.scanClasses(classes)
        def globalScope = new GlobalScope(name, resourceRoot, castingDirector, eventBus)
        globalScope.start()
        globalScope
    }

}
