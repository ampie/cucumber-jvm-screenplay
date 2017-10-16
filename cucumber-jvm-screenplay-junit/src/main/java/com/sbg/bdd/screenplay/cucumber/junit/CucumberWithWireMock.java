package com.sbg.bdd.screenplay.cucumber.junit;

import com.sbg.bdd.cucumber.screenplay.core.formatter.CucumberScreenplayLifecycleSync;
import com.sbg.bdd.cucumber.screenplay.scoped.plugin.CucumberScopeLifecycleSync;
import com.sbg.bdd.cucumber.screenplay.scoped.plugin.GlobalScopeBuilder;
import com.sbg.bdd.cucumber.wiremock.annotations.ResourceRoots;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayWireMockConfig;
import com.sbg.bdd.cucumber.wiremock.memorizer.CucumberWireMockConfigurator;
import com.sbg.bdd.wiremock.scoped.common.Reflection;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.junit.Assertions;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitOptions;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;

import static com.sbg.bdd.wiremock.scoped.common.Reflection.getValue;


public class CucumberWithWireMock extends ParentRunner<FeatureRunner> {
    private final JUnitReporter jUnitReporter;
    private final List<FeatureRunner> children = new ArrayList<FeatureRunner>();
    private final Runtime runtime;

    public CucumberWithWireMock(Class clazz) throws InitializationError, IOException {
        super(clazz);
        ClassLoader classLoader = clazz.getClassLoader();
        if(!clazz.isAnnotationPresent(ScreenplayWireMockConfig.class)){
            throw new IllegalStateException("Please annotate your JUnit class with the ScreenplayWireMockConfig annotation");
        }
        CucumberWireMockConfigurator configurator = new CucumberWireMockConfigurator((ScreenplayWireMockConfig) clazz.getAnnotation(ScreenplayWireMockConfig.class));
        GlobalScopeBuilder.configureWith(configurator);
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        ensureLifecycleSyncPluginApplied(runtimeOptions);
        ResourceLoader resourceLoader = new GenericResourceMultiLoader(configurator.getFeatureFileRoot(), classLoader);
        runtime = createRuntime(resourceLoader, classLoader, runtimeOptions);
        if(runtimeOptions.getFeaturePaths().isEmpty() ||  (runtimeOptions.getFeaturePaths().size() == 1 && runtimeOptions.getFeaturePaths().get(0).startsWith("classpath:"))){
            //No ResourceRoot based dir specified
            runtimeOptions.getFeaturePaths().add("/");
        }
        final JUnitOptions junitOptions = new JUnitOptions(runtimeOptions.getJunitOptions());
        final List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
        jUnitReporter = new JUnitReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.isStrict(), junitOptions);
        addChildren(cucumberFeatures);
    }

    private void ensureLifecycleSyncPluginApplied(RuntimeOptions runtimeOptions) {
        List<String> plugins = getValue(runtimeOptions,"pluginFormatterNames");
        boolean found = false;
        for (String plugin : plugins) {
            if(plugin.equals(CucumberScopeLifecycleSync.class.getName()) || (plugin.equals(CucumberScreenplayLifecycleSync.class.getName()))){
                found =true;
            }
        }
        if(!found){
            plugins.add(CucumberScopeLifecycleSync.class.getName());
        }
    }


    protected Runtime createRuntime(ResourceLoader resourceLoader, ClassLoader classLoader,
                                    RuntimeOptions runtimeOptions) throws InitializationError, IOException {
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        return new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
    }

    @Override
    public List<FeatureRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(FeatureRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(FeatureRunner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        jUnitReporter.done();
        jUnitReporter.close();
        runtime.printSummary();
    }

    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            children.add(new FeatureRunner(cucumberFeature, runtime, jUnitReporter));
        }
    }

}
