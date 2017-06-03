package com.sbg.bdd.cucumber.screenplay.scoped

import com.sbg.bdd.cucumber.screenplay.scoped.plugin.ScopingPlugin
import com.sbg.bdd.resource.file.RootDirectoryResource
import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement
import com.sbg.bdd.screenplay.core.annotations.SceneEventType
import com.sbg.bdd.screenplay.core.internal.BaseActor
import cucumber.runtime.*
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.java.JavaBackend
import groovy.json.JsonSlurper
import spock.lang.Specification

import static java.util.Arrays.asList

class WhenRunningWithScopes  extends Specification{



    def 'the correct event should be fired in a predictable  sequence'() {
        given:
        BaseActor.useStopWatch(new StopWatchStub(9999))
        when:
        def report = null;
        try {
            report = runFeaturesWithScreenplayPlugin(asList("classpath:cucumber/scoping/ScopedStuff.feature"));
        } catch (Exception e) {
            e.printStackTrace()
        }
        then:
        report.size() == 1
        report[0].elements.size() == 2
        report[0].elements[0].steps.size() == 1
        StepDefs.SCOPE_CALLBACKS.keySet().size()==5
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][0] == SceneEventType.BEFORE_START
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][1] == SceneEventType.AFTER_START
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][2] == SceneEventType.BEFORE_COMPLETE
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber'][3] == SceneEventType.AFTER_COMPLETE
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber/scoping'].size() == 4
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber/scoping'][0] == SceneEventType.BEFORE_START
        StepDefs.SCOPE_CALLBACKS['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else'][0] == SceneEventType.BEFORE_START
        StepDefs.USER_CALLBACKS.keySet().size()==5
        StepDefs.USER_CALLBACKS['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else/John'][0] == ActorInvolvement.BEFORE_ENTER_STAGE
        StepDefs.USER_CALLBACKS['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else/John'][1] == ActorInvolvement.AFTER_ENTER_STAGE
        StepDefs.VARIABLE_AFTER_COMPLETE.keySet().size()==5
        StepDefs.VARIABLE_AFTER_START['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else']==4
        StepDefs.VARIABLE_AFTER_COMPLETE['RunAll/cucumber/scoping/Basic_Screen_Flow/Flow_through_the_screens_something_else']==4//Because we retain the everybody scope's memory
        println( StepDefs.SCOPE_CALLBACKS.keySet())

    }

    def runFeaturesWithScreenplayPlugin(List<String> featurePaths) throws IOException {
        def report = File.createTempFile("cucumber-scope", ".json");
        def classLoader = Thread.currentThread().getContextClassLoader();
        def markerFile = new File(classLoader.getResource('cucumber-jvm-screenplay-scoped-marker.txt').file)

        def inputResourceRoot = new RootDirectoryResource(markerFile.getParentFile())
        def outputResourceRoot = new RootDirectoryResource(new File(markerFile.getParentFile().getParentFile().getParentFile(),"output_root"))
        BuildIt.useResourceRoots(inputResourceRoot,outputResourceRoot)
        def resourceLoader = new MultiLoader(classLoader)
        def args = new ArrayList<String>()
        args.add("--plugin")
        args.add(ScopingPlugin.class.getName() +  ":" + report.getAbsolutePath())
        args.add("--glue")
        args.add(StepDefs.class.getPackage().getName())
        args.addAll(featurePaths)

        RuntimeOptions runtimeOptions = new RuntimeOptions(args)

        Backend backend = new JavaBackend(resourceLoader)
        RuntimeGlue glue=null//new RuntimeGlue();
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(backend), runtimeOptions, new StopWatch.Stub(1234), glue)
        runtime.run()
        def actual = new Scanner(report, "UTF-8").useDelimiter("\\A").next()
        def jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(actual)

    }
}