package cucumber.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import cucumber.screenplay.DownstreamStub;


public interface ExtendedMappingBuilder extends MappingBuilder, DownstreamStub {
    ExtendedMappingBuilder withRequestBody(StringValuePattern... patterns);

    ExtendedMappingBuilder recordingResponses();

    ExtendedMappingBuilder recordingResponsesTo(String recordingDirectory);

    ExtendedMappingBuilder to(String urlInfo);

    ExtendedMappingBuilder toAnyKnownExternalService();

    ExtendedMappingBuilder will(ResponseStrategy responseStrategy);

    ExtendedMappingBuilder to(ResponseStrategy responseStrategy);

    ExtendedMappingBuilder to(String urlInfo, String pathSuffix);


    void changeUrlToPattern();

    ExtendedMappingBuilder mapsToJournalDirectory(String journalDirectory);

    ExtendedMappingBuilder playingBackResponsesFrom(String recordingDirectory);

    ExtendedMappingBuilder playingBackResponses();

    boolean isToAllKnownExternalServices();

    void addChildBuilder(ExtendedMappingBuilder newBuilder);

    String getUrlInfo();

    RecordingSpecification getRecordingSpecification();

    ExtendedRequestPatternBuilder getRequestPatternBuilder();

    ExtendedResponseDefinitionBuilder getResponseDefinitionBuilder();
}
