package com.sbg.bdd.cucumber.screenplay.scoped

import com.sbg.bdd.cucumber.screenplay.scoped.plugin.ScopingPlugin
import com.sbg.bdd.resource.file.DirectoryResourceRoot
import cucumber.runtime.*
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.java.JavaBackend
import groovy.json.JsonSlurper
import spock.lang.Specification

import static java.util.Arrays.asList

abstract class WhenScopeXYZ extends Specification {
    def runFeaturesWithScreenplayPlugin(List<String> featurePaths) throws IOException {
        def report = File.createTempFile('cucumber-scope', '.json');
        def classLoader = Thread.currentThread().getContextClassLoader();
        def markerFile = new File(classLoader.getResource('cucumber-jvm-screenplay-scoped-marker.txt').file)

        def inputResourceRoot = new DirectoryResourceRoot('input', markerFile.getParentFile())
        def outputResourceRoot = new DirectoryResourceRoot('output', new File(markerFile.getParentFile().getParentFile().getParentFile(), 'output_root'))
        BuildIt.useResourceRoots(inputResourceRoot, outputResourceRoot)
        def resourceLoader = new MultiLoader(classLoader)
        def args = new ArrayList<String>()
        args.add('--plugin')
        args.add(ScopingPlugin.class.getName() + ':' + report.getAbsolutePath())
        args.add('--glue')
        args.add(StepDefs.class.getPackage().getName())
        args.addAll(featurePaths)

        RuntimeOptions runtimeOptions = new RuntimeOptions(args)

        Backend backend = new JavaBackend(resourceLoader)
        RuntimeGlue glue = null//new RuntimeGlue();
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(backend), runtimeOptions, new StopWatch.Stub(1234), glue)
        runtime.run()
        def actual = new Scanner(report, 'UTF-8').useDelimiter('\\A').next()
        def jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(actual)

    }
}
