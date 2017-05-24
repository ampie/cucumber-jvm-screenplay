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


    public static ResponseStrategy returnTheBody(final String body, final String contentType) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext scope) throws Exception {
                builder.atPriority(scope.calculatePriority(3));
                return aResponse().withBody(body).withHeader("Content-StepEventType", contentType);
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
                ExtendedResponseDefinitionBuilder responseBuilder = aResponse().withBody(responseBody).withHeader("Content-StepEventType", determineContentType(fileName));
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
                ExtendedResponseDefinitionBuilder responseBuilder = aResponse().withBody(responseBody).withHeader("Content-StepEventType", determineContentType(templateBuilder.getFileName()));
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

    public static ExtendedResponseDefinitionBuilder aResponse() {
        return new ExtendedResponseDefinitionBuilder();
    }
}
