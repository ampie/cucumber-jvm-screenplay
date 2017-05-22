package cucumber.scoping

import cucumber.runtime.Backend
import cucumber.runtime.Runtime
import cucumber.runtime.RuntimeGlue
import cucumber.runtime.RuntimeOptions
import cucumber.runtime.StopWatch
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.java.JavaBackend
import cucumber.scoping.annotations.ScopePhase
import cucumber.scoping.annotations.UserInvolvement
import cucumber.scoping.glue.StepDefs
import cucumber.scoping.plugin.ScopingFormatter
import cucumber.screenplay.formatter.BaseActor
import groovy.json.JsonSlurper
import spock.lang.Specification

import static java.util.Arrays.asList


class WhenRunningWithScopes  extends Specification{
    def 'the correct event should be fired in a predictable  sequence'() {
        given:
        BaseActor.useStopWatch(new StopWatch.Stub(9999))
        when:
        def report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/scoping/ScopedStuff.feature"));
        then:
        report.size() == 1
        report[0].elements.size() == 2
        report[0].elements[0].steps.size() == 1
        StepDefs.SCOPE_CALLBACKS.keySet().size()==5
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][0] == ScopePhase.BEFORE_START
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][1] == ScopePhase.AFTER_START
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][2] == ScopePhase.BEFORE_COMPLETE
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][3] == ScopePhase.AFTER_COMPLETE
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber/scoping'].size() == 4
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber/scoping'][0] == ScopePhase.BEFORE_START
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else'][0] == ScopePhase.BEFORE_START
        StepDefs.USER_CALLBACKS.keySet().size()==5
        StepDefs.USER_CALLBACKS['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else/John'][0] == UserInvolvement.BEFORE_ENTER
        StepDefs.USER_CALLBACKS['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else/John'][1] == UserInvolvement.AFTER_ENTER
        StepDefs.VARIABLE_AFTER_COMPLETE.keySet().size()==5
        StepDefs.VARIABLE_AFTER_START['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else']==4
        StepDefs.VARIABLE_AFTER_COMPLETE['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else']==3
        println( StepDefs.SCOPE_CALLBACKS.keySet())

    }

    def runFeaturesWithScreenplayPlugin(List<String> featurePaths) throws IOException {
        def report = File.createTempFile("cucumber-scope", ".json");
        def classLoader = Thread.currentThread().getContextClassLoader();
        def resourceLoader = new MultiLoader(classLoader);
        def args = new ArrayList<String>();
        args.add("--plugin");
        args.add(ScopingFormatter.class.getName() +  ":" + report.getAbsolutePath());
        args.add("--glue");
        args.add(StepDefs.class.getPackage().getName());
        args.addAll(featurePaths);

        RuntimeOptions runtimeOptions = new RuntimeOptions(args);

        Backend backend = new JavaBackend(resourceLoader);
        RuntimeGlue glue=null;//new RuntimeGlue();
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(backend), runtimeOptions, new StopWatch.Stub(1234), glue);
        runtime.run();
        def actual = new Scanner(report, "UTF-8").useDelimiter("\\A").next();
        System.out.println(actual);
        def jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(actual)

    }
}