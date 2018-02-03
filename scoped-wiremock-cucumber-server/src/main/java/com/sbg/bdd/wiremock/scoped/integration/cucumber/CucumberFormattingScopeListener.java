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
    public static final String SERENITY_JIRA_INTEGRATION_ENABLED = "serenity.jira.integration.enabled";
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
    public void globalScopeStarted(CorrelationState knownScope) {
        String[] segments = knownScope.getCorrelationPath().split("\\/");
        this.runName = segments[2];
        out = new StringWriter();
        reporter = new ScreenPlayFormatter(out);
        this.parser = new MapParser(reporter, reporter);
    }

    @Override
    public void nestedScopeStarted(CorrelationState knownScope) {
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
    public void nestedScopeStopped(CorrelationState state) {

    }

    @Override
    public void globalScopeStopped(CorrelationState state) {
        reporter.done();
        admin.getResourceRoot("outputResourceRoot").resolvePotential(runName + ".json").write(out.toString().getBytes());
        File localDir = makeResourcesAvailableOnLocalFileSystem(admin.getResourceRoot("outputResourceRoot"));
        if(state.getPayload()!=null && state.getPayload().containsKey(SERENITY_JIRA_INTEGRATION_ENABLED)){
            System.setProperty(SERENITY_JIRA_INTEGRATION_ENABLED,state.getPayload().get(SERENITY_JIRA_INTEGRATION_ENABLED).toString());
        }
        publishSerenityResults(localDir);
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
            System.setProperty(ThucydidesSystemProperty.ENABLE_MARKDOWN.getPropertyName(), "story,narrative,scenario,step");
            System.setProperty("serenity.reports.show.step.details", "true");
            //TODO parameterise
            System.setProperty("serenity.public.url", "http://172.99.0.5:8080");
            System.setProperty("serenity.report.show.manual.tests", "false");
            System.setProperty("serenity.issue.tracker.url", "https://tools.standardbank.co.za/jira/browse/{0}");
            System.setProperty("jira.url", "https://tools.standardbank.co.za/jira");
            File[] reportFiles = inputDir.listFiles();
            File outputDir = new File("/wiremock/__files");
            FilterChainConfig config = new FilterChainConfig();
            List<InputLinkConfig> inputs = new ArrayList<>();
            config.setInputs(inputs);
            List<ProcessorLinkConfig> processors = new ArrayList<>();
            config.setProcessors(processors);
            List<String> contextualizerNames = addInputsAndContextualizers(reportFiles, inputs, processors);
            addRequirementsTragger(processors, contextualizerNames);
            String lastProcessorName = maybeAddJiraUpdatingProcessor(processors);
            config.setOutputs(buildOutputs(outputDir,lastProcessorName));
//            PublishingLinkConfig publishingLinkConfig = new PublishingLinkConfig("webdav", WebDavPublishingStrategy.class.getName(), webDavDestination, Arrays.asList("html", "aggregate-html"));
//            publishingLinkConfig.setProperties(new Properties());
//            publishingLinkConfig.getProperties().setProperty("username", "test");
//            publishingLinkConfig.getProperties().setProperty("password", "test");
//            config.setPublishers(Arrays.asList(publishingLinkConfig));
            writeOutput(config);
            publishResults(config);
            System.out.println("Serenity results published successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishResults(FilterChainConfig config) {
        for (PublishingLink p : config.getPublishingLinks()) {
//                System.out.println("Publishing to " + webDavDestination);
            p.publish();
        }
    }

    private void writeOutput(FilterChainConfig config) {
        for (OutputLink outputLink : config.buildLinks()) {
            outputLink.clean();
        }
        for (OutputLink outputLink : config.buildLinks()) {
            System.out.println("Generating output: " + outputLink.getImplementation().getClass().getName());
            System.out.println(" to  " + outputLink.getOutputDirectory());
            outputLink.write();
        }
    }

    private List<OutputLinkConfig> buildOutputs(File outputDir, String lastProcessorName) {
        OutputLinkConfig output1LinkConfig = new OutputLinkConfig("aggregate-html", AggregateHtmlOutput.class.getName(), outputDir, Arrays.asList(lastProcessorName));
        OutputLinkConfig output2LinkConfig = new OutputLinkConfig("html", StandardSerenityOutput.class.getName(), outputDir, Arrays.asList(lastProcessorName));
        return Arrays.asList(output1LinkConfig, output2LinkConfig);
    }

    private void addRequirementsTragger(List<ProcessorLinkConfig> processors, List<String> contextualizerNames) {
        processors.add(new ProcessorLinkConfig("requirementsTagger", RequirementsTaggingProcessor.class.getName(), contextualizerNames));
    }

    private List<String> addInputsAndContextualizers(File[] reportFiles, List<InputLinkConfig> inputs, List<ProcessorLinkConfig> processors) {
        List<String> contextualizerNames = new ArrayList<>();
        for (File reportFile : reportFiles) {
            if (reportFile.isFile() && reportFile.getName().endsWith(".json")) {
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
        return contextualizerNames;
    }

    private String maybeAddJiraUpdatingProcessor(List<ProcessorLinkConfig> processors) {
        if(true || "true".equals(System.getProperty(SERENITY_JIRA_INTEGRATION_ENABLED))) {
            System.setProperty("serenity.jira.workflow", "cli-workflow.groovy");
            System.setProperty("jira.root.issue.type", "\"Technical Issue\"");
            System.setProperty("jira.root.issue.additional.jql", "labels = CSF AND labels = Test");
            System.setProperty("jira.requirement.links", "sub-task");
            System.setProperty("jira.project", "PW");
            System.setProperty("jira.username", "a230787");
            System.setProperty("jira.password", "ainnikki");
            System.setProperty("serenity.jira.workflow.active", "true");
            System.setProperty("serenity.skip.jira.updates", "false");
            processors.add(new ProcessorLinkConfig("jira-updater", JiraUpdatingProcessor.class.getName(), Arrays.asList("requirementsTagger")));
            return "jira-updater";
        }else{
            return "requirementsTagger";
        }
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
                } else if ("step".equals(payload.get("method"))) {
                    parser.replayStep(payload);
                } else if ("stepAndMatch".equals(payload.get("method"))) {
                    parser.replayStepAndMatch(payload);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
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
                } else if ("matchAndResult".equals(payload.get("method"))) {
                    parser.replayMatchAndResult(payload);
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
