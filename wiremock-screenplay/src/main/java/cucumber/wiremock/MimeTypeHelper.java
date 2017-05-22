package cucumber.wiremock;

import com.github.tomakehurst.wiremock.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.Iterator;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;


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

        /**
         * <code>&#42;/*</code>
         */
        ANY("*/*"),
        /**
         * <code>text/plain</code>
         */
        TEXT("text/plain"),
        /**
         * <ul>
         * <li><code>application/json</code></li>
         * <li><code>application/javascript</code></li>
         * <li><code>text/javascript</code></li>
         * </ul>
         */
        JSON("application/json", "application/javascript", "text/javascript"),
        /**
         * <ul>
         * <li><code>application/xml</code></li>
         * <li><code>text/xml</code></li>
         * <li><code>application/xhtml+xml</code></li>
         * </ul>
         */
        XML("application/xml", "text/xml", "application/xhtml+xml"),
        /**
         * <code>text/html</code>
         */
        HTML("text/html"),
        /**
         * <code>application/x-www-form-urlencoded</code>
         */
        URLENC("application/x-www-form-urlencoded"),
        /**
         * <code>application/octet-stream</code>
         */
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

        /**
         * Builds a string to be used as an HTTP <code>Accept</code> header
         * value, i.e. "application/xml, text/xml"
         *
         * @return
         */
        @SuppressWarnings("unchecked")
        public String getAcceptHeader() {
            Iterator<String> iter = asList(ctStrings).iterator();
            StringBuilder sb = new StringBuilder();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) sb.append(", ");
            }
            return sb.toString();
        }

        /**
         * Specify a charset for this content-type
         *
         * @param charset The charset
         * @return The content-type with the given charset.
         */
        public String withCharset(Charset charset) {
            if (charset == null) {
                throw new IllegalArgumentException("charset cannot be null");
            }
            return withCharset(charset.toString());
        }

        /**
         * Specify a charset for this content-type
         *
         * @param charset The charset
         * @return The content-type with the given charset.
         */
        public String withCharset(String charset) {
            if (StringUtils.isBlank(charset)) {
                throw new IllegalArgumentException("charset cannot be empty");
            }
            return format("%s; charset=%s", this.toString(), trim(charset));
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
