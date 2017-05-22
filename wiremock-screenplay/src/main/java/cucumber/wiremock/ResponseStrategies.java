package cucumber.wiremock;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static cucumber.wiremock.MimeTypeHelper.determineContentType;


public class ResponseStrategies {
    public static ResponseStrategy mapToJournalDirectory(final String journalDirectory) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.mapsToJournalDirectory(journalDirectory);
                builder.atPriority(scope.calculatePriority(1));
                return null;
            }
        };
    }


    public static ResponseStrategy playbackResponsesFrom(final String recordingDirectory) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.changeUrlToPattern();
                builder.playingBackResponsesFrom(recordingDirectory);
                builder.atPriority(scope.calculatePriority(2));
                return null;
            }
        };
    }

    public static ResponseStrategy playbackResponses() {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.changeUrlToPattern();
                builder.playingBackResponses();
                builder.atPriority(scope.calculatePriority(2));
                return null;
            }
        };
    }

    public static ResponseStrategy recordResponsesTo(final String recordingDirectory) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.changeUrlToPattern();
                builder.recordingResponsesTo(recordingDirectory);
                builder.atPriority(scope.calculatePriority(2));
                return null;
            }
        };
    }

    public static ResponseStrategy recordResponses() {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.changeUrlToPattern();
                builder.recordingResponses();
                builder.atPriority(scope.calculatePriority(2));
                return null;
            }
        };
    }

    public static ResponseStrategy returnTheBody(final String body, final String contentType) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.atPriority(scope.calculatePriority(3));
                return aResponse().withBody(body).withHeader("Content-Type", contentType);
            }
        };
    }

    public static ResponseStrategy returnTheFile(final String fileName) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                File bodyFile;
                if (new File(fileName).isAbsolute()) {
                    bodyFile = new File(fileName);
                } else {
                    bodyFile = scope.resolveResource(fileName);
                }
                String responseBody = FileUtils.readFileToString(bodyFile);
                String headers = readHeaders(bodyFile);
                builder.atPriority(scope.calculatePriority(3));
                ExtendedResponseDefinitionBuilder responseBuilder = aResponse().withBody(responseBody).withHeader("Content-Type", determineContentType(fileName));
                if (headers != null) {
                    addHeaders(headers, responseBuilder);
                }
                return responseBuilder;
            }
        };
    }

    public static ResponseStrategy merge(final TemplateBuilder templateBuilder) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                File templateFile;
                if (new File(templateBuilder.getFileName()).isAbsolute()) {
                    templateFile = new File(templateBuilder.getFileName());
                } else {
                    templateFile = scope.resolveResource(templateBuilder.getFileName());
                }
                String templateContent = FileUtils.readFileToString(templateFile);
                String headers = readHeaders(templateFile);

                Handlebars mf = new Handlebars();
                Template mustache = mf.compileInline(templateContent);
                StringBuilderWriter writer = new StringBuilderWriter();
                mustache.apply(templateBuilder.getVariables(), writer);
                String responseBody = writer.toString();

                builder.atPriority(scope.calculatePriority(3));
                ExtendedResponseDefinitionBuilder responseBuilder = aResponse().withBody(responseBody).withHeader("Content-Type", determineContentType(templateBuilder.getFileName()));
                if (headers != null) {
                    addHeaders(headers, responseBuilder);
                }
                return responseBuilder;
            }
        };
    }

    public static void addHeaders(String headers, ResponseDefinitionBuilder responseDefinitionBuilder) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = (ObjectNode) mapper.readTree(headers);
            Iterator<Map.Entry<String, JsonNode>> fields = jsonObject.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                responseDefinitionBuilder.withHeader(entry.getKey(), entry.getValue().asText());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String readHeaders(File templateFile) throws IOException {
        String baseName = templateFile.getName().substring(0, templateFile.getName().lastIndexOf('.'));
        File headersFile = new File(templateFile.getParentFile(), baseName + ".headers.json");
        if (headersFile.exists()) {
            return FileUtils.readFileToString(headersFile);
        } else {
            return null;
        }
    }

    public static TemplateBuilder theTemplate(String templateFileName) {
        return new TemplateBuilder(templateFileName);

    }

    public static ResponseStrategy targetTheJaxrsApplication(String jaxrsApplicationRoot) {
        return useTheLastTwoSegmentsAndTarget(jaxrsApplicationRoot);
    }

    public static ResponseStrategy targetTheServiceUnderTest() {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.changeUrlToPattern();
                builder.atPriority(scope.calculatePriority(4));
                return aResponse().proxiedFrom(scope.getBaseUrlOfServiceUnderTest())
                        .withTransformers("ProxyUrlTransformer").withTransformerParameter("segmentsToUse", 2).withTransformerParameter("useTrailingSegments", true);
            }
        };
    }

    public static ResponseStrategy useTheLastSegmentAndTarget(String baseUrl) {
        return willTargetTheServiceUsingTheLastSegments(baseUrl, 1);
    }

    public static ResponseStrategy useTheLastTwoSegmentsAndTarget(String baseUrl) {
        return willTargetTheServiceUsingTheLastSegments(baseUrl, 2);
    }


    public static ResponseStrategy useTheLastThreeSegmentsAndTarget(String baseUrl) {
        return willTargetTheServiceUsingTheLastSegments(baseUrl, 3);
    }

    public static ResponseStrategy willTargetTheServiceUsingTheLastSegments(final String baseUrl, final int segments) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.changeUrlToPattern();
                builder.atPriority(scope.calculatePriority(4));
                return aResponse().proxiedFrom(baseUrl).withTransformers("ProxyUrlTransformer").withTransformerParameter("segmentsToUse", segments).withTransformerParameter("useTrailingSegments", true);
            }
        };
    }

    public static ResponseStrategy proxyTo(final String baseUrl) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.changeUrlToPattern();
                builder.atPriority(scope.calculatePriority(5));
                return aResponse().proxiedFrom(baseUrl);
            }
        };
    }

    public static ExtendedResponseDefinitionBuilder aResponse() {
        return new ExtendedResponseDefinitionBuilder();
    }

    public static ResponseStrategy beIntercepted() {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.atPriority(scope.calculatePriority(5));
                builder.changeUrlToPattern();
                return aResponse().interceptedFromSource();
            }
        };

    }

}
