package cucumber.wiremock;

import com.github.ampie.wiremock.common.Reflection;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collection;

import static com.github.ampie.wiremock.common.Reflection.getValue;
import static com.github.ampie.wiremock.common.Reflection.setValue;


public class ExtendedResponseDefinitionBuilder extends ResponseDefinitionBuilder.ProxyResponseDefinitionBuilder  {
    private boolean interceptedFromSource;

    public ExtendedResponseDefinitionBuilder() {
        super(new ResponseDefinitionBuilder());
    }
    public ExtendedResponseDefinitionBuilder(ExtendedResponseDefinitionBuilder from) {
        super(from);
        this.interceptedFromSource=from.interceptedFromSource;
        Reflection.setValue(this,"additionalRequestHeaders",new ArrayList<>((Collection<? extends HttpHeader>) Reflection.getValue(from,"additionalRequestHeaders")));
    }

    public ExtendedResponseDefinitionBuilder(ResponseDefinitionBuilder builder) {
        super(builder);
    }

    public ExtendedResponseDefinitionBuilder interceptedFromSource(){
        this.interceptedFromSource=true;
        return this;
    }
    public boolean interceptFromSource(){
        return this.interceptedFromSource;
    }
    @Override
    public ExtendedResponseDefinitionBuilder proxiedFrom(String proxyBaseUrl) {
        this.proxyBaseUrl = proxyBaseUrl;
        return this;
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withAdditionalRequestHeader(String key, String value) {
        return (ExtendedResponseDefinitionBuilder)super.withAdditionalRequestHeader(key, value);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withStatus(int status) {
        return (ExtendedResponseDefinitionBuilder)super.withStatus(status);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withHeader(String key, String... values) {
        return (ExtendedResponseDefinitionBuilder)super.withHeader(key, values);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withBody(String body) {
        return (ExtendedResponseDefinitionBuilder)super.withBody(body);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withFault(Fault fault) {
        return (ExtendedResponseDefinitionBuilder)super.withFault(fault);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withStatusMessage(String message) {
        return (ExtendedResponseDefinitionBuilder)super.withStatusMessage(message);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withBase64Body(String base64Body) {
        return (ExtendedResponseDefinitionBuilder)super.withBase64Body(base64Body);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withHeaders(HttpHeaders headers) {
        return (ExtendedResponseDefinitionBuilder)super.withHeaders(headers);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withTransformer(String transformerName, String parameterKey, Object parameterValue) {
        return (ExtendedResponseDefinitionBuilder)super.withTransformer(transformerName, parameterKey, parameterValue);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withTransformerParameter(String name, Object value) {
        return (ExtendedResponseDefinitionBuilder)super.withTransformerParameter(name, value);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withTransformers(String... responseTransformerNames) {
        return (ExtendedResponseDefinitionBuilder)super.withTransformers(responseTransformerNames);
    }
    
    @Override
    public ExtendedResponseDefinitionBuilder withFixedDelay(Integer milliseconds) {
        return (ExtendedResponseDefinitionBuilder)super.withFixedDelay(milliseconds);
    }
}
