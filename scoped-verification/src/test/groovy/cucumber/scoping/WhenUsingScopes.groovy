package cucumber.scoping

import cucumber.scoping.events.ScreenplayLifecycleSync
import cucumber.scoping.persona.CharacterType
import cucumber.scoping.persona.Persona
import cucumber.screenplay.Actor
import cucumber.screenplay.Task
import cucumber.screenplay.annotations.Step
import cucumber.screenplay.internal.InstanceGetter
import cucumber.scoping.events.ScopeEventBus
import cucumber.scoping.events.properties.PropertiesPersonaClient
import cucumber.screenplay.actors.OnStage
import spock.lang.Specification
import static cucumber.screenplay.actors.OnStage.*
import static cucumber.screenplay.ScreenplayPhrases.*
import static cucumber.scoping.ScopingPhrases.*

import java.nio.file.Paths

abstract class WhenUsingScopes extends Specification {

    def buildGlobalScope(String name, Class<?>... glue) {
        def resourceRoot = Paths.get('src/test/resources')
        def eventBus = new ScopeEventBus(Mock(InstanceGetter) {
            getInstance(_) >> { args -> args[0].newInstance() }
        })

        def castingDirector = new ScopedCastingDirector(eventBus, new PropertiesPersonaClient(), resourceRoot)
        def classes = new HashSet<Class<?>>(Arrays.asList(glue))
        classes.add(ScreenplayLifecycleSync)
        eventBus.scanClasses(classes)
        def globalScope = new GlobalScope(name, resourceRoot, castingDirector, eventBus)
        globalScope.start()
        globalScope
    }

}
