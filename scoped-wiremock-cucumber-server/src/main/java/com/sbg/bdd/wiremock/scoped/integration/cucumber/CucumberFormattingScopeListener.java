package com.sbg.bdd.wiremock.scoped.integration.cucumber;

import com.github.tomakehurst.wiremock.common.Json;
import com.google.common.io.Files;
import com.sbg.bdd.cucumber.screenplay.core.formatter.MapParser;
import com.sbg.bdd.cucumber.screenplay.core.formatter.ScreenPlayFormatter;
import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.Resource;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.resource.file.DirectoryResource;
import com.sbg.bdd.resource.file.DirectoryResourceRoot;
import com.sbg.bdd.wiremock.scoped.admin.ScopedAdmin;
import com.sbg.bdd.wiremock.scoped.admin.model.CorrelationState;
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedExchange;
import com.sbg.bdd.wiremock.scoped.server.ScopeListener;
import gherkin.deps.net.iharder.Base64;
import net.serenitybdd.cucumber.filterchain.*;
import net.serenitybdd.cucumber.filterchain.inputs.CucumberImporter;
import net.serenitybdd.cucumber.filterchain.integrators.JiraUpdatingProcessor;
import net.serenitybdd.cucumber.filterchain.modifiers.ContextTaggingProcessor;
import net.serenitybdd.cucumber.filterchain.modifiers.RequirementsTaggingProcessor;
import net.serenitybdd.cucumber.filterchain.outputs.AggregateHtmlOutput;
import net.serenitybdd.cucumber.filterchain.outputs.StandardSerenityOutput;
import net.thucydides.core.ThucydidesSystemProperty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class CucumberFormattingScopeListener implements ScopeListener {
    private static final Logger LOGGER = Logger.getLogger(CucumberFormattingScopeListener.class.getName());
    private ScreenPlayFormatter reporter;
    private MapParser parser;
    private ScopedAdmin admin;
    private StringWriter out;
    private String runName;

    public CucumberFormattingScopeListener() {
    }

    @Override
    public void setScopedAdmin(ScopedAdmin admin) {
        this.admin = admin;

    }

    @Override
    public void scopeStarted(CorrelationState knownScope) {
        String[] segments = knownScope.getCorrelationPath().split("\\/");
        if (segments.length == 4) {//ip/port/name/runid
            this.runName = segments[2];
            out = new StringWriter();
            reporter = new ScreenPlayFormatter(out);
            this.parser = new MapParser(reporter, reporter);
        }
        Map<String, Object> payload = knownScope.getPayload();
        if (payload != null) {
            if ("feature".equals(payload.get("method"))) {
                parser.replayFeature(payload);
            } else if ("featureElement".equals(payload.get("method"))) {
                parser.replayFeatureElement(payload);
            }
        }
    }

    @Override
    public void scopeStopped(CorrelationState state) {
        if (state.getCorrelationPath().split("\\/").length == 4) {//ip/port/name/runid
            reporter.done();
            admin.getResourceRoot("outputResourceRoot").resolvePotential(runName + ".json").write(out.toString().getBytes());
            File localDir = makeResourcesAvailableOnLocalFileSystem(admin.getResourceRoot("outputResourceRoot"));
            publishSerenityResults(localDir);
        }
    }

    public static void main(String[] args) {
        CucumberFormattingScopeListener listener = new CucumberFormattingScopeListener();
        listener.publishSerenityResults(listener.makeResourcesAvailableOnLocalFileSystem(new DirectoryResourceRoot("asdf", new File("/home/ampie/Code/card/card-output/"))));
        System.exit(0);
    }

    private void publishSerenityResults(File inputDir) {
        try {
            System.setProperty(ThucydidesSystemProperty.THUCYDIDES_REQUIREMENTS_DIR.getPropertyName(), "/wiremock/input/features");
            System.setProperty(ThucydidesSystemProperty.THUCYDIDES_REQUIREMENT_TYPES.getPropertyName(), "capability,feature,low level feature");
            File[] reportFiles = inputDir.listFiles();
            File outputDir = new File("/wiremock/__files");
            FilterChainConfig config = new FilterChainConfig();
            List<InputLinkConfig> inputs = new ArrayList<>();
            config.setInputs(inputs);
            List<ProcessorLinkConfig> processors = new ArrayList<>();
            config.setProcessors(processors);
            List<String> contextualizerNames = new ArrayList<>();
            for (File reportFile : reportFiles) {
                if (reportFile.isFile()) {
                    InputLinkConfig inputLinkConfig = new InputLinkConfig(reportFile.getName() + "-json", CucumberImporter.class.getName(), Paths.get(reportFile.getAbsolutePath()));
                    inputs.add(inputLinkConfig);
                    ProcessorLinkConfig contextualizer = new ProcessorLinkConfig(reportFile.getName() + "-contextualizer", ContextTaggingProcessor.class.getName(), Arrays.asList(reportFile.getName() + "-json"));
                    processors.add(contextualizer);
                    contextualizer.setProperties(new Properties());
                    String runName = reportFile.getName().substring(0, reportFile.getName().indexOf("."));
                    if (runName.indexOf("_") > 0) {
                        contextualizer.getProperties().put("sourceContext", runName.split("\\_")[0]);
                        contextualizer.getProperties().put("scenarioStatus", runName.split("\\_")[1]);
                    } else {
                        contextualizer.getProperties().put("sourceContext", runName);
                        contextualizer.getProperties().put("scenarioStatus", runName);
                    }
                    contextualizerNames.add(contextualizer.getName());
                }
            }
            processors.add(new ProcessorLinkConfig("requirementsTagger", RequirementsTaggingProcessor.class.getName(), contextualizerNames));
//            addJirUpdatingProcessor(processors);
            OutputLinkConfig output1LinkConfig = new OutputLinkConfig("aggregate-html", AggregateHtmlOutput.class.getName(), outputDir, Arrays.asList("jira-updater"));
            OutputLinkConfig output2LinkConfig = new OutputLinkConfig("html", StandardSerenityOutput.class.getName(), outputDir, Arrays.asList("requirementsTagger"));
            config.setOutputs(Arrays.asList(output1LinkConfig, output2LinkConfig));
//            PublishingLinkConfig publishingLinkConfig = new PublishingLinkConfig("webdav", WebDavPublishingStrategy.class.getName(), webDavDestination, Arrays.asList("html", "aggregate-html"));
//            publishingLinkConfig.setProperties(new Properties());
//            publishingLinkConfig.getProperties().setProperty("username", "test");
//            publishingLinkConfig.getProperties().setProperty("password", "test");
//            config.setPublishers(Arrays.asList(publishingLinkConfig));
            for (OutputLink outputLink : config.buildLinks()) {
                outputLink.clean();
            }
            for (OutputLink outputLink : config.buildLinks()) {
                System.out.println("Generating output: " + outputLink.getImplementation().getClass().getName());
                System.out.println(" to  " + outputLink.getOutputDirectory());
                outputLink.write();
            }
            for (PublishingLink p : config.getPublishingLinks()) {
//                System.out.println("Publishing to " + webDavDestination);
                p.publish();
            }
            System.out.println("Serenity results published successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addJirUpdatingProcessor(List<ProcessorLinkConfig> processors) {
        System.setProperty("serenity.jira.workflow","cli-workflow.groovy");
        System.setProperty("serenity.reports.show.step.details","true");
        System.setProperty("serenity.report.show.manual.tests","false");
        System.setProperty("serenity.issue.tracker.url","http://jira.standardbank.co.za:8091/browse/{0}");
        System.setProperty("jira.root.issue.type","\"Technical Issue\"");
        System.setProperty("jira.root.issue.additional.jql","labels = CSF AND labels = Test");
        System.setProperty("jira.requirement.links","sub-task");
        System.setProperty("jira.url","http://jira.standardbank.co.za:8091");
        System.setProperty("jira.project","PW");
        System.setProperty("jira.username","ampie.barnard");
        System.setProperty("jira.password","ainnikki");
        System.setProperty("serenity.jira.workflow.active","true");
        System.setProperty("serenity.skip.jira.updates","false");
        System.setProperty("serenity.public.url","http://172.99.0.5:8080");
        processors.add(new ProcessorLinkConfig("jira-updater", JiraUpdatingProcessor.class.getName(), Arrays.asList("requirementsTagger")));
    }

    private File makeResourcesAvailableOnLocalFileSystem(ResourceContainer outputResourceRoot) {
        try {
            if (outputResourceRoot instanceof DirectoryResource) {
                return ((DirectoryResource) outputResourceRoot).getFile();
            } else {
                File tmpDir = Files.createTempDir();
                Resource[] reports = outputResourceRoot.list();
                for (Resource report : reports) {
                    if (report instanceof ReadableResource) {
                        FileOutputStream fos = new FileOutputStream(new File(tmpDir, report.getName()));
                        fos.write(((ReadableResource) report).read());
                        fos.close();
                    }
                }
                return tmpDir;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stepStarted(CorrelationState state) {
        try {
            Map<String, Object> payload = state.getPayload();
            if (payload != null && !payload.isEmpty()) {
                if ("childStepAndMatch".equals(payload.get("method"))) {
                    parser.replayChildStepAndMatch(payload);
                } else if ("stepAndMatch".equals(payload.get("method"))) {
                    parser.replayStepAndMatch(payload);
                }
            }
        } catch (Exception e) {
            //be proactive
            LOGGER.warning(e.toString());
        }
    }

    @Override
    public void stepCompleted(CorrelationState state) {
        try {
            Map<String, Object> payload = state.getPayload();
            if (payload != null && !payload.isEmpty()) {
                List<Map<String, Object>> embeddings = (List<Map<String, Object>>) payload.get("embeddings");
                if (embeddings != null) {
                    for (Map<String, Object> embedding : embeddings) {
                        parser.replayEmbedding(embedding);
                    }
                }
                //TODO find Mappings that were made during the step - maintain a map of them in the currentScopestate
                List<RecordedExchange> exchanges = admin.findExchangesAgainstStep(state.getCorrelationPath(), state.getCurrentStep());
                if (exchanges.size() > 0) {
                    Map<String, Object> embedding = new HashMap<>();
                    embedding.put("mime_type", "application/json");
                    embedding.put("data", Base64.encodeBytes(Json.write(exchanges).getBytes()));
                    parser.replayEmbedding(embedding);
                }
                if ("childResult".equals(payload.get("method"))) {
                    parser.replayChildResult(payload);
                } else if ("result".equals(payload.get("method"))) {
                    parser.replayResult(payload);
                }
            }
        } catch (Exception e) {
            //be proactive
            LOGGER.warning(e.toString());
        }
    }

    @Override
    public String getName() {
        return "CucumberFormattingScopeListener";
    }
}
