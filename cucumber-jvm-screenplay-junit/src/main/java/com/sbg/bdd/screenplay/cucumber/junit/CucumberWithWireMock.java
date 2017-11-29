package com.sbg.bdd.screenplay.cucumber.junit;

import com.sbg.bdd.cucumber.screenplay.core.formatter.CucumberScreenplayLifecycleSync;
import com.sbg.bdd.cucumber.screenplay.scoped.plugin.CucumberScopeLifecycleSync;
import com.sbg.bdd.cucumber.screenplay.scoped.plugin.GlobalScopeBuilder;
import com.sbg.bdd.cucumber.wiremock.annotations.ResourceRoots;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayWireMockConfig;
import com.sbg.bdd.cucumber.wiremock.memorizer.CucumberWireMockConfigurator;
import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.wiremock.WireMockMemories;
import com.sbg.bdd.wiremock.scoped.common.Reflection;
import com.sbg.bdd.wiremock.scoped.resources.WireMockResourceRoot;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.junit.Assertions;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitOptions;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Tag;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;

import static com.sbg.bdd.wiremock.scoped.common.Reflection.getValue;


public class CucumberWithWireMock extends ParentRunner<FeatureRunner> {
    private final JUnitReporter jUnitReporter;
    private final List<FeatureRunner> children = new ArrayList<FeatureRunner>();
    private final Runtime runtime;
    private final ScreenplayWireMockConfig screenplayWireMockConfig;

    public CucumberWithWireMock(Class clazz) throws InitializationError, IOException {
        super(clazz);
        runInitializers(clazz);
        ClassLoader classLoader = clazz.getClassLoader();
        if (!clazz.isAnnotationPresent(ScreenplayWireMockConfig.class)) {
            throw new IllegalStateException("Please annotate your JUnit class with the ScreenplayWireMockConfig annotation");
        }
        screenplayWireMockConfig = (ScreenplayWireMockConfig) clazz.getAnnotation(ScreenplayWireMockConfig.class);
        CucumberWireMockConfigurator configurator = new CucumberWireMockConfigurator(screenplayWireMockConfig);
        GlobalScopeBuilder.configureWith(configurator);
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        ensureLifecycleSyncPluginApplied(runtimeOptions);
        ResourceLoader resourceLoader = new GenericResourceMultiLoader(configurator.getFeatureFileRoot(), classLoader);
        runtime = createRuntime(resourceLoader, classLoader, runtimeOptions);
        if (runtimeOptions.getFeaturePaths().isEmpty() || (runtimeOptions.getFeaturePaths().size() == 1 && runtimeOptions.getFeaturePaths().get(0).startsWith("classpath:"))) {
            //No ResourceRoot based dir specified
            runtimeOptions.getFeaturePaths().add("/");
        }
        final JUnitOptions junitOptions = new JUnitOptions(runtimeOptions.getJunitOptions());
        final List<CucumberFeature> cucumberFeatures = filterFeaturesAndScenarios(runtimeOptions.cucumberFeatures(resourceLoader), screenplayWireMockConfig);
        jUnitReporter = new JUnitReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.isStrict(), junitOptions);
        addChildren(cucumberFeatures);
    }

    private void runInitializers(Class clazz) {
        try {
            clazz.newInstance();
        } catch (Exception e) {

        }
    }

    public static List<CucumberFeature> filterFeaturesAndScenarios(List<CucumberFeature> cucumberFeatures, ScreenplayWireMockConfig screenplayWireMockConfig) {
        String[] tags = screenplayWireMockConfig.tags();
        if (tags != null && tags.length > 0) {
            Iterator<CucumberFeature> iterator = cucumberFeatures.iterator();
            while (iterator.hasNext()) {
                if (shouldIgnore(iterator.next(), tags)) {
                    iterator.remove();
                }
            }
        }
        return cucumberFeatures;
    }

    private static  boolean shouldIgnore(CucumberFeature next, String[] tags) {
        boolean hasTagMatch = hasTagMatch(tags, next.getGherkinFeature().getTags());
        Iterator<CucumberTagStatement> iterator = next.getFeatureElements().iterator();
        while (iterator.hasNext()) {
            if (!hasTagMatch(tags, iterator.next().getGherkinModel().getTags())) {
                iterator.remove();
            }
        }
        return hasTagMatch && next.getFeatureElements().size() > 0;
    }

    private static  boolean hasTagMatch(String[] tags, List<Tag> tags1) {
        boolean hasTagMatch = false;
        outer:
        for (Tag tag : tags1) {
            for (String s : tags) {
                if (Pattern.compile(s).matcher(tag.getName()).find()) {
                    hasTagMatch = true;
                    break outer;
                }
            }
        }
        return hasTagMatch;
    }

    private void ensureLifecycleSyncPluginApplied(RuntimeOptions runtimeOptions) {
        List<String> plugins = getValue(runtimeOptions, "pluginFormatterNames");
        boolean found = false;
        for (String plugin : plugins) {
            if (plugin.equals(CucumberScopeLifecycleSync.class.getName()) || (plugin.equals(CucumberScreenplayLifecycleSync.class.getName()))) {
                found = true;
            }
        }
        if (!found) {
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
        saveCucumberReport();
    }

    private void saveCucumberReport() {
        try {
            ResourceContainer outputRoot = WireMockMemories.recallFor(OnStage.performance()).theOutputResourceRoot();
            ReadableResource report = (ReadableResource) outputRoot.resolveExisting(OnStage.performance().getName() + ".json");
            File outputFile = new File(this.screenplayWireMockConfig.resultDir(), report.getName());
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(report.read());
                fos.flush();
            }
        } catch (Exception e) {
            Logger logger = Logger.getLogger(CucumberWithWireMock.class.getName());
            logger.log(Level.WARNING,"Could not save report: " + e.toString(),e);
        }
    }


    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            children.add(new FeatureRunner(cucumberFeature, runtime, jUnitReporter));
        }
    }

}
