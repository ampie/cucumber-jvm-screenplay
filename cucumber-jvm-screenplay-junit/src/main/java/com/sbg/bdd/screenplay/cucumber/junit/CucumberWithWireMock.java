package com.sbg.bdd.screenplay.cucumber.junit;

import com.sbg.bdd.cucumber.wiremock.annotations.ResourceRoots;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayWireMockConfig;
import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.ResourceContainer;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.*;
import cucumber.runtime.junit.Assertions;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitOptions;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;


public class CucumberWithWireMock extends ParentRunner<FeatureRunner> {
    private final JUnitReporter jUnitReporter;
    private final List<FeatureRunner> children = new ArrayList<FeatureRunner>();
    private final Runtime runtime;

    public CucumberWithWireMock(Class clazz) throws InitializationError, IOException {
        super(clazz);
        ClassLoader classLoader = clazz.getClassLoader();
        ResourceRoots resourceRoots = getResourceRoots(clazz);
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        ResourceLoader resourceLoader = new WireMockMultiLoader(resourceRoots.getFeatureFileRoot(), classLoader);
        runtime = createRuntime(resourceLoader, classLoader, runtimeOptions);

        final JUnitOptions junitOptions = new JUnitOptions(runtimeOptions.getJunitOptions());
        final List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
        jUnitReporter = new JUnitReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.isStrict(), junitOptions);
        addChildren(cucumberFeatures);
    }

    private ResourceRoots getResourceRoots(Class<?> clazz) {
        try {
            ScreenplayWireMockConfig annotation = clazz.getAnnotation(ScreenplayWireMockConfig.class);
            if(annotation==null || annotation.resourceRoots() == ResourceRoots.class){
                throw new IllegalStateException("Please provide a ResourceRoots implementation by annotating your JUnit test class with '" + ScreenplayWireMockConfig.class.getName() + "'");
            }
            return annotation.resourceRoots().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
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

    public static class WireMockMultiLoader extends MultiLoader {
        private ResourceContainer root;

        public WireMockMultiLoader(ResourceContainer root, ClassLoader cl) {
            super(cl);
            this.root = root;
        }

        @Override
        public Iterable<Resource> resources(String path, String suffix) {
            List<Resource> result = new ArrayList<>();
            if (path.startsWith(CLASSPATH_SCHEME) || !suffix.endsWith(".feature")) {
                for (Resource resource : super.resources(path, suffix)) {
                    result.add(resource);
                }
            } else {
                com.sbg.bdd.resource.Resource existing = root.resolveExisting(path);
                if (existing != null) {
                    addRecursively(suffix, existing, result);
                }
            }
            return result;
        }

        private void addRecursively(String suffix, com.sbg.bdd.resource.Resource existing, List<Resource> result) {
            if (existing instanceof ResourceContainer) {
                for (com.sbg.bdd.resource.Resource child : ((ResourceContainer) existing).list()) {
                    addRecursively(suffix, child, result);
                }
            } else if (existing.getName().endsWith(suffix)) {
                result.add(new WireMockResourceAdapter(root, existing));
            }
        }
    }

    public static class WireMockResourceAdapter implements Resource {
        private com.sbg.bdd.resource.Resource delegate;
        private ResourceContainer cucumberResourceRoot;

        public WireMockResourceAdapter(ResourceContainer cucumberResourceRoot, com.sbg.bdd.resource.Resource resource) {
            delegate = resource;
            this.cucumberResourceRoot = cucumberResourceRoot;
        }

        @Override
        public String getPath() {
            String path = delegate.getPath().substring(cucumberResourceRoot.getPath().length());
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        }

        @Override
        public String getAbsolutePath() {
            return getPath();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(((ReadableResource) delegate).read());
        }

        @Override
        public String getClassName(String extension) {
            return getPath().substring(0, getPath().length() - extension.length()).replace('/', '.');
        }
    }

}
