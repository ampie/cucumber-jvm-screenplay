package cucumber.screenplay.formatter

import cucumber.runtime.*
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.java.JavaBackend
import cucumber.screenplay.TaskSteps
import groovy.json.JsonSlurper
import spock.lang.Specification

import static java.util.Arrays.asList

abstract class WhenPerformingChildSteps extends Specification{

    def runFeaturesWithScreenplayPlugin(List<String> featurePaths) throws IOException {
        def report = File.createTempFile("cucumber-screenplay", ".json");
        def classLoader = Thread.currentThread().getContextClassLoader();
        def resourceLoader = new MultiLoader(classLoader);
        def args = new ArrayList<String>();
        args.add("--plugin");
        args.add(ScreenPlayFormatter.class.getName() +  ":" + report.getAbsolutePath());
        args.add("--glue");
        args.add(TaskSteps.class.getPackage().getName());
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
