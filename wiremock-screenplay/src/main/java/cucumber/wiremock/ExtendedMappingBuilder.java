package cucumber.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import cucumber.screenplay.DownstreamStub;


public interface ExtendedMappingBuilder extends MappingBuilder, DownstreamStub {
    ExtendedMappingBuilder to(String urlInfo);

    ExtendedMappingBuilder will(ResponseStrategy responseStrategy);

    ExtendedMappingBuilder to(ResponseStrategy responseStrategy);


    void addChildBuilder(ExtendedMappingBuilder newBuilder);

    String getUrlInfo();

    RecordingSpecification getRecordingSpecification();

    ExtendedRequestPatternBuilder getRequestPatternBuilder();

    ExtendedResponseDefinitionBuilder getResponseDefinitionBuilder();
}
