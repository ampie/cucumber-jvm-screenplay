package cucumber.wiremock;

import com.github.tomakehurst.wiremock.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;


public class MimeTypeHelper {
    public static String calculateExtension(HttpHeaders headers) {
        String contentType = headers.getContentTypeHeader().firstValue();
        ContentType type = ContentType.fromContentType(contentType);
        type = type == null ? ContentType.TEXT : type;
        String extension = ".any";
        switch (type) {
            case JSON:
                extension = ".json";
                break;
            case XML:
                extension = ".xml";
                break;
            case TEXT:
                extension = ".txt";
                break;
            case BINARY:
                extension = ".bin";
                break;
        }
        return extension;
    }

    public static String determineContentType(String bodyContentFile) {
        if (bodyContentFile.endsWith(".xml")) {
            return ContentType.XML.getContentTypeStrings()[1];
        } else if (bodyContentFile.endsWith(".json")) {
            return ContentType.JSON.getContentTypeStrings()[0];
        } else {
            return ContentType.TEXT.getContentTypeStrings()[0];
        }
    }

    public static enum ContentType {

        ANY("*/*"),
        TEXT("text/plain"),
        JSON("application/json", "application/javascript", "text/javascript"),
        XML("application/xml", "text/xml", "application/xhtml+xml"),
        HTML("text/html"),
        URLENC("application/x-www-form-urlencoded"),
        BINARY("application/octet-stream");

        private static final String PLUS_XML = "+xml";
        private static final String PLUS_JSON = "+json";
        private static final String PLUS_HTML = "+html";

        private final String[] ctStrings;

        public String[] getContentTypeStrings() {
            return ctStrings;
        }

        @Override
        public String toString() {
            return ctStrings[0];
        }

        private ContentType(String... contentTypes) {
            this.ctStrings = contentTypes;
        }

        public static ContentType fromContentType(String contentType) {
            if (contentType == null) {
                return null;
            }
            contentType = getContentTypeWithoutCharset(contentType.toLowerCase());
            final ContentType foundContentType;
            if (contains(XML.ctStrings, contentType) || endsWithIgnoreCase(contentType, PLUS_XML)) {
                foundContentType = XML;
            } else if (contains(JSON.ctStrings, contentType) || endsWithIgnoreCase(contentType, PLUS_JSON)) {
                foundContentType = JSON;
            } else if (contains(TEXT.ctStrings, contentType)) {
                foundContentType = TEXT;
            } else if (contains(HTML.ctStrings, contentType) || endsWithIgnoreCase(contentType, PLUS_HTML)) {
                foundContentType = HTML;
            } else {
                foundContentType = null;
            }
            return foundContentType;
        }

        private static String getContentTypeWithoutCharset(String contentType) {
            return StringUtils.trim(StringUtils.substringBefore(contentType, ";"));
        }

    }

}
