package cucumber.wiremock;

public interface ResponseStrategy {
    ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception;
}
