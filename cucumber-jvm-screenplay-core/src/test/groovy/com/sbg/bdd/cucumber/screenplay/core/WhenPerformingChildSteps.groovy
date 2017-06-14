package com.sbg.bdd.cucumber.screenplay.core

import com.sbg.bdd.cucumber.screenplay.core.formatter.SimpleScreenplayPlugin
import com.sbg.bdd.resource.file.DirectoryResourceRoot
import cucumber.runtime.*
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.java.JavaBackend
import groovy.json.JsonSlurper
import spock.lang.Specification

import static java.util.Arrays.asList

abstract class WhenPerformingChildSteps extends Specification {

    def runFeaturesWithScreenplayPlugin(List<String> featurePaths) throws IOException {
        def report = File.createTempFile("cucumber-screenplay", ".json");
        def classLoader = Thread.currentThread().getContextClassLoader();
        def markerFile = new File(classLoader.getResource("cucumber-jvm-screenplay-core-marker.txt").file)
        def outputDir = new File(markerFile.getParentFile().getParentFile().getParentFile(), "screenplay_output")
        outputDir.mkdirs()
        SamplePerformanceBuilder.useResourceRoots(new DirectoryResourceRoot('input', markerFile.getParentFile()), new DirectoryResourceRoot('output', outputDir))
        def resourceLoader = new MultiLoader(classLoader);
        def args = new ArrayList<String>();
        args.add("--plugin");
        args.add(SimpleScreenplayPlugin.class.getName() + ":" + report.getAbsolutePath());
        args.add("--glue");
        args.add(TaskSteps.class.getPackage().getName());
        args.addAll(featurePaths);

        RuntimeOptions runtimeOptions = new RuntimeOptions(args);

        Backend backend = new JavaBackend(resourceLoader);
        RuntimeGlue glue = null;//new RuntimeGlue();
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(backend), runtimeOptions, new StopWatch.Stub(1234), glue);
        runtime.run();
        def actual = new Scanner(report, "UTF-8").useDelimiter("\\A").next();
        def jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(actual)

    }
}
