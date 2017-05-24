package cucumber.wiremock;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;


public abstract class BaseHttpClient {
    protected static Map<String, Integer> statusMap = new HashMap<>();
    private CloseableHttpClient httpClient;

    static {
        statusMap.put("POST", HttpURLConnection.HTTP_CREATED);
        statusMap.put("DELETE", HttpURLConnection.HTTP_NO_CONTENT);
        statusMap.put("GET", HttpURLConnection.HTTP_OK);
        statusMap.put("PUT", HttpURLConnection.HTTP_OK);
    }

    public BaseHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ObjectNode execute(HttpRequestBase request) throws IOException {
        CloseableHttpResponse response = httpClient.execute(request);
        if (response == null) {
            throw new IllegalStateException(request.getURI() + " " + request.getMethod() + " not mocked!");
        }
        String resultString = getResponseString(response);
        Integer status = statusMap.get(request.getMethod());
        if (response.getStatusLine().getStatusCode() == status || response.getStatusLine().getStatusCode() == 200) {
            if (resultString == null || resultString.length() == 0) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            return (ObjectNode) mapper.readTree(resultString);
        } else {
            throw new IllegalArgumentException(response.getStatusLine().getReasonPhrase() + ": " + resultString);
        }

    }

    private String getResponseString(CloseableHttpResponse response) throws IOException {
        String string = getEntityAsStringAndCloseStream(response);
        response.close();
        return string;
    }

    private static String getEntityAsStringAndCloseStream(HttpResponse httpResponse) {
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            try {
                String content = EntityUtils.toString(entity, "UTF-8");
                entity.getContent().close();
                return content;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
        return null;
    }
}
