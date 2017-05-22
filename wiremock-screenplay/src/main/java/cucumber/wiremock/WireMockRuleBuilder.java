package cucumber.wiremock;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.ScenarioMappingBuilder;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import cucumber.scoping.UserInScope;
import cucumber.screenplay.ActorOnStage;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WireMockRuleBuilder implements ExtendedMappingBuilder {
    private ExtendedRequestPatternBuilder requestPatternBuilder;
    private ExtendedResponseDefinitionBuilder responseDefinitionBuilder;
    private RecordingSpecification recordingSpecification;
    private String name;
    private UUID id;
    private Integer priority;
    private List<WireMockRuleBuilder> children = new ArrayList<>();
    private Boolean persistent;
    private ResponseStrategy responseStrategy;

    public WireMockRuleBuilder(String method) {
        this.requestPatternBuilder = new ExtendedRequestPatternBuilder(RequestMethod.fromString(method));
    }

    public WireMockRuleBuilder(WireMockContext verificationContext, String method) {
        this.requestPatternBuilder = new ExtendedRequestPatternBuilder(verificationContext, RequestMethod.fromString(method));
    }

    public WireMockRuleBuilder(ExtendedRequestPatternBuilder requestPatternBuilder, ExtendedResponseDefinitionBuilder responseDefinitionBuilder, RecordingSpecification recordingSpecification) {
        this(requestPatternBuilder);
        if (responseDefinitionBuilder != null) {
            this.responseDefinitionBuilder = new ExtendedResponseDefinitionBuilder(responseDefinitionBuilder);
        }
        if (recordingSpecification != null) {
            this.recordingSpecification = new RecordingSpecification(recordingSpecification);
        }
    }

    public WireMockRuleBuilder(ExtendedRequestPatternBuilder requestPatternBuilder) {
        this.requestPatternBuilder = new ExtendedRequestPatternBuilder(requestPatternBuilder);
    }

    public boolean enforceJournalModeInScope() {
        return getRecordingSpecification().enforceJournalModeInScope();
    }

    public boolean recordToCurrentResourceDir() {
        return getRecordingSpecification().recordToCurrentResourceDir();
    }


    @Override
    public WireMockRuleBuilder withRequestBody(StringValuePattern bodyPattern) {
        this.requestPatternBuilder.withRequestBody(bodyPattern);
        return this;
    }

    @Override
    public ScenarioMappingBuilder inScenario(String scenarioName) {
        throw new IllegalStateException("Scenarios not supported");
    }

    @Override
    public MappingBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public MappingBuilder withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public MappingBuilder persistent() {
        this.persistent = true;
        return this;
    }

    @Override
    public MappingBuilder withBasicAuth(String username, String password) {
        this.requestPatternBuilder.withBasicAuth(new BasicCredentials(username, password));
        return this;
    }

    @Override
    public MappingBuilder withCookie(String name, StringValuePattern cookieValuePattern) {
        this.requestPatternBuilder.withCookie(name, cookieValuePattern);
        return this;
    }

    @Override
    public <P> MappingBuilder withPostServeAction(String extensionName, P parameters) {
        throw new IllegalStateException("PostServeActions not supported");
    }

    public WireMockRuleBuilder withRequestBody(StringValuePattern... bodyPattern) {
        for (StringValuePattern stringValuePattern : bodyPattern) {
            withRequestBody(stringValuePattern);
        }
        return this;
    }

    @Override
    public WireMockRuleBuilder withHeader(String key, StringValuePattern headerPattern) {
        this.requestPatternBuilder.withHeader(key, headerPattern);
        return this;
    }

    @Override
    public MappingBuilder withQueryParam(String key, StringValuePattern queryParamPattern) {
        this.requestPatternBuilder.withQueryParam(key, queryParamPattern);
        return this;
    }

    @Override
    public WireMockRuleBuilder recordingResponses() {
        getRecordingSpecification().recordingResponses();
        return this;
    }

    @Override
    public WireMockRuleBuilder recordingResponsesTo(String recordingDirectory) {
        getRecordingSpecification().recordingResponsesTo(recordingDirectory);
        return this;
    }


    public WireMockRuleBuilder playingBackResponses() {
        getRecordingSpecification().playbackResponses();
        return this;
    }

    public WireMockRuleBuilder playingBackResponsesFrom(String recordingDirectory) {
        getRecordingSpecification().playbackResponsesFrom(recordingDirectory);
        return this;
    }

    public void changeUrlToPattern() {
        this.requestPatternBuilder.changeUrlToPattern();
    }


    @Override
    public WireMockRuleBuilder mapsToJournalDirectory(String journalDirectory) {
        getRecordingSpecification().mapsToJournalDirectory(journalDirectory);
        this.changeUrlToPattern();
        return this;
    }

    @Override
    public WireMockRuleBuilder to(String urlInfo) {
        this.requestPatternBuilder.to(urlInfo);
        return this;
    }

    @Override
    public WireMockRuleBuilder toAnyKnownExternalService() {
        this.requestPatternBuilder.toAnyKnownExternalService();
        return this;
    }

    @Override
    public ExtendedMappingBuilder will(ResponseStrategy responseStrategy) {
        return to(responseStrategy);
    }

    @Override
    public WireMockRuleBuilder to(ResponseStrategy responseStrategy) {
        try {
            this.responseStrategy = responseStrategy;
            return this;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void createChildren(WireMockContext verificationContext) {
        if (requestPatternBuilder.isToAllKnownExternalServices()) {
            Map<String, String> allEndpoints = verificationContext.allKnownExternalEndpoints();
            for (Map.Entry<String, String> entry : allEndpoints.entrySet()) {
                String urlOfSource = entry.getValue();
                try {
                    URL url = new URL(urlOfSource);
                    WireMockRuleBuilder newBuilder = new WireMockRuleBuilder(requestPatternBuilder, responseDefinitionBuilder, recordingSpecification);
                    newBuilder.to(url.getPath() + ".*");
                    if (responseDefinitionBuilder != null && responseDefinitionBuilder.interceptFromSource()) {
                        String proxiedBaseUrl = url.getProtocol() + "://" + url.getAuthority();
                        newBuilder.getResponseDefinitionBuilder().proxiedFrom(proxiedBaseUrl);
                    }
                    addChildBuilder(newBuilder.atPriority(getPriority()));
                } catch (MalformedURLException e) {
                    System.out.println("Could not load knownExternalEndpoint '" + entry.getKey() + "'='" + entry.getValue() + "'");
                }

            }
        } else if (responseDefinitionBuilder != null && responseDefinitionBuilder.interceptFromSource()) {
            URL url = verificationContext.endpointUrlFor(getUrlInfo());
            String proxiedBaseUrl = url.getProtocol() + "://" + url.getAuthority();
            responseDefinitionBuilder.proxiedFrom(proxiedBaseUrl);
        }

    }


    @Override
    public WireMockRuleBuilder to(String urlInfo, String pathSuffix) {
        this.requestPatternBuilder.to(urlInfo, pathSuffix);
        return this;
    }

    @Override
    public WireMockRuleBuilder atPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public Integer getPriority() {
        return priority;
    }


    public ExtendedRequestPatternBuilder getRequestPatternBuilder() {
        return requestPatternBuilder;
    }

    public ExtendedResponseDefinitionBuilder getResponseDefinitionBuilder() {
        return responseDefinitionBuilder;
    }

    public JournalMode getJournalModeOverride() {
        return getRecordingSpecification().getJournalModeOverride();
    }

    public String getUrlInfo() {
        return this.requestPatternBuilder.getUrlInfo();
    }

    public String getRecordingDirectory() {
        return getRecordingSpecification().getRecordingDirectory();
    }

    public boolean isToAllKnownExternalServices() {
        return this.requestPatternBuilder.isToAllKnownExternalServices();
    }

    public void addChildBuilder(ExtendedMappingBuilder newBuilder) {
        this.children.add((WireMockRuleBuilder) newBuilder);
    }

    public WireMockRuleBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder) {
        if (responseDefBuilder instanceof ExtendedResponseDefinitionBuilder) {
            this.responseDefinitionBuilder = (ExtendedResponseDefinitionBuilder) responseDefBuilder;
        } else if (responseDefBuilder != null) {
            this.responseDefinitionBuilder = new ExtendedResponseDefinitionBuilder(responseDefBuilder);
        }
        if (this.responseDefinitionBuilder != null) {


        }
        return this;
    }

    @Override
    public StubMapping build() {
        RequestPattern requestPattern = requestPatternBuilder.build();
        ResponseDefinition response = responseDefinitionBuilder.build();
        StubMapping mapping = new StubMapping(requestPattern, response);
        mapping.setPriority(priority);
        mapping.setUuid(id);
        mapping.setName(name);
        mapping.setPersistent(persistent);
        return mapping;
    }


    public RecordingSpecification getRecordingSpecification() {
        if (recordingSpecification == null) {
            recordingSpecification = new RecordingSpecification();
        }
        return recordingSpecification;
    }


    @Override
    public void performOnStage(ActorOnStage actorOnStage) {
        WireMockContext verificationContext = new WireMockContext((UserInScope) actorOnStage);
        if (responseDefinitionBuilder == null) {
            try {
                responseDefinitionBuilder = responseStrategy.applyTo(this, verificationContext);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        //This may not have been created by one of the known ResponseStrategies, so let's just give it a default priority and assume the user wants this as a priority
        if (getPriority() == null && verificationContext != null) {
            atPriority(verificationContext.calculatePriority(1));
        }
        createChildren(verificationContext);
        if (children.size() > 0) {
            for (WireMockRuleBuilder child : children) {
                verificationContext.register(child);
            }
        } else {
            verificationContext.register(this);
        }
    }
}
