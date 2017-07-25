package com.sbg.bdd.wiremock.scoped.integration.cucumber;

import com.github.tomakehurst.wiremock.common.Json;
import com.google.common.io.Files;
import com.sbg.bdd.cucumber.screenplay.core.formatter.MapParser;
import com.sbg.bdd.cucumber.screenplay.core.formatter.ScreenPlayFormatter;
import com.sbg.bdd.wiremock.scoped.admin.ScopedAdmin;
import com.sbg.bdd.wiremock.scoped.admin.model.CorrelationState;
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedExchange;
import com.sbg.bdd.wiremock.scoped.server.ScopeListener;
import gherkin.deps.net.iharder.Base64;
import net.serenitybdd.cucumber.filterchain.*;
import net.serenitybdd.cucumber.filterchain.inputs.CucumberImporter;
import net.serenitybdd.cucumber.filterchain.outputs.AggregateHtmlOutput;
import net.serenitybdd.cucumber.filterchain.outputs.StandardSerenityOutput;
import net.serenitybdd.cucumber.filterchain.publishers.WebDavPublishingStrategy;

import java.io.File;
import java.io.FileWriter;
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

    public CucumberFormattingScopeListener() {
    }

    @Override
    public void setScopedAdmin(ScopedAdmin admin) {
        this.admin = admin;

    }

    @Override
    public void scopeStarted(CorrelationState knownScope) {
        if (knownScope.getCorrelationPath().split("\\/").length == 4) {//ip/port/name/runid
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
            admin.getResourceRoot("outputResourceRoot").resolvePotential("cucumber.json").write(out.toString().getBytes());
            File tmpFile = writeOutputToTempFile();
            publishSerenityResults("http://webdav:80/webdav", tmpFile);
        }
    }

    public static void main(String[] args) {
        new CucumberFormattingScopeListener().publishSerenityResults("http://localhost:8084/webdav",new File("/home/ampie/Code/card/card-output/cucumber.json"));
//        System.exit(0);
    }
    private void publishSerenityResults(String webDavDestination, File tmpFile) {
        try {
            File tmpDir = Files.createTempDir();
//            new File("target/site/serenity").mkdirs();
            InputLinkConfig inputLinkConfig = new InputLinkConfig("cucumber-json", CucumberImporter.class.getName(), Paths.get(tmpFile.getAbsolutePath()));
            OutputLinkConfig output1LinkConfig = new OutputLinkConfig("aggregate-html", AggregateHtmlOutput.class.getName(), tmpDir, Arrays.asList("cucumber-json"));
            OutputLinkConfig output2LinkConfig = new OutputLinkConfig("html", StandardSerenityOutput.class.getName(), tmpDir, Arrays.asList("cucumber-json"));
            PublishingLinkConfig publishingLinkConfig = new PublishingLinkConfig("webdav", WebDavPublishingStrategy.class.getName(), webDavDestination, Arrays.asList("html","aggregate-html"));
            publishingLinkConfig.setProperties(new Properties());
            publishingLinkConfig.getProperties().setProperty("username","test");
            publishingLinkConfig.getProperties().setProperty("password","test");
            FilterChainConfig config = new FilterChainConfig();
            config.setInputs(Arrays.asList(inputLinkConfig));
            config.setOutputs(Arrays.asList(output1LinkConfig,output2LinkConfig));
            config.setPublishers(Arrays.asList(publishingLinkConfig));
            for (OutputLink outputLink : config.buildLinks()) {
                System.out.println("Generating output: " + outputLink.getImplementation().getClass().getName());
                System.out.println(" to  " + outputLink.getOutputDirectory());
                outputLink.write();
            }
            for (PublishingLink p: config.getPublishingLinks()) {
                System.out.println("Publishing to " + webDavDestination);
                p.publish();
            }
            System.out.println("Serenity results published successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File writeOutputToTempFile() {
        try {
            File tmpFile = File.createTempFile("cucumber", ".json");
            FileWriter fileWriter = new FileWriter(tmpFile);
            fileWriter.write(out.toString());
            fileWriter.close();
            return tmpFile;
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
