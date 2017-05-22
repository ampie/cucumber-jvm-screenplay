package cucumber.wiremock;

import com.github.ampie.wiremock.common.HeaderName;
import com.github.ampie.wiremock.common.Reflection;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.*;
import cucumber.screenplay.ActorOnStage;
import cucumber.screenplay.DownstreamStub;
import cucumber.screenplay.DownstreamVerification;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//import cucumber.scoping.UserInScope;


public class ExtendedRequestPatternBuilder extends RequestPatternBuilder {
    private String urlInfo;
    private String pathSuffix;
    private boolean urlIsPattern = false;
    private boolean toAllKnownExternalServices = false;
    private UrlPattern urlPattern;


    public ExtendedRequestPatternBuilder(ExtendedRequestPatternBuilder builder) {
        super();
        copyFrom(builder);
    }

    public ExtendedRequestPatternBuilder(RequestMethod requestMethod) {
        super(requestMethod, null);
    }

    private void copyFrom(ExtendedRequestPatternBuilder builder) {
        this.urlInfo = builder.urlInfo;
        this.pathSuffix = builder.pathSuffix;
        this.urlIsPattern = builder.urlIsPattern;
        this.toAllKnownExternalServices = builder.toAllKnownExternalServices;
        Reflection.setValue(this, "method", Reflection.getValue(builder, "method"));
        Reflection.setValue(this, "headers", new HashMap<>((Map<? extends String, ? extends MultiValuePattern>) Reflection.getValue(builder, "headers")));
        Reflection.setValue(this, "queryParams", new HashMap<>((Map<? extends String, ? extends MultiValuePattern>) Reflection.getValue(builder, "queryParams")));
        Reflection.setValue(this, "bodyPatterns", new ArrayList<>((Collection<? extends MultiValuePattern>) Reflection.getValue(builder, "bodyPatterns")));
        Reflection.setValue(this, "cookies", new HashMap<>((Map<? extends String, ? extends StringValuePattern>) Reflection.getValue(builder, "cookies")));
        Reflection.setValue(this, "basicCredentials", Reflection.getValue(builder, "basicCredentials"));
    }

    public ExtendedRequestPatternBuilder(ExtendedRequestPatternBuilder requestPatternBuilder, RequestMethod method) {
        super(method, null);
        copyFrom(requestPatternBuilder);
    }


    public ExtendedRequestPatternBuilder toAnyKnownExternalService() {
        toAllKnownExternalServices = true;
        urlInfo = ".*";
        return this;
    }

    @Override
    public ExtendedRequestPatternBuilder withRequestBody(StringValuePattern valuePattern) {
        return (ExtendedRequestPatternBuilder) super.withRequestBody(valuePattern);
    }

    public boolean isToAllKnownExternalServices() {
        return this.toAllKnownExternalServices;
    }

    public void changeUrlToPattern() {
        urlIsPattern = true;
    }
    
    public ExtendedRequestPatternBuilder to(String urlInfo, String pathSuffix) {
        this.urlInfo = urlInfo;
        this.pathSuffix = pathSuffix;
        return this;
    }

    public ExtendedRequestPatternBuilder to(String urlInfo) {
        return to(urlInfo, null);
    }

    public UrlPattern getUrlPathPattern() {
        return urlPattern;
    }

    private UrlPattern calculateUrlPattern(EndpointPropertyResolver verificationContext) {
        String path = this.urlInfo;
        if (isPropertyName(path)) {
            try {
                URL uri = verificationContext.endpointUrlFor(path);
                path = uri.getPath();
            } catch (Exception e) {
                System.out.println(e);
                //TODO Think about this
            }
        }
        if (pathSuffix != null) {
            path = path + this.pathSuffix;
        }
        if (this.urlIsPattern && !path.endsWith(".*")) {
            path = path + ".*";
        }
        if (path.contains(".*")) {
            return new UrlPattern(new RegexPattern(path), true);
        } else {
            return new UrlPattern(new EqualToPattern(path), false);
        }
    }
    
    private boolean isPropertyName(String p) {
        return p.matches("[_a-zA-Z0-9\\.]+");
    }

    public String getUrlInfo() {
        return urlInfo;
    }

    public String getHttpMethod() {
        return ((RequestMethod) Reflection.getValue(this, "method")).getName();

    }

    public void ensureScopePath(StringValuePattern pattern) {
        Map<String, MultiValuePattern> headers = Reflection.getValue(this, "headers");
        if (!headers.containsKey(HeaderName.ofTheCorrelationKey())) {
            withHeader(HeaderName.ofTheCorrelationKey(), pattern);
        }
    }

    public WireMockRuleBuilder will(ResponseStrategy strategy) {
        WireMockRuleBuilder ruleBuilder = new WireMockRuleBuilder(this);
        ruleBuilder.will(strategy);
        return ruleBuilder;
    }

    public DownstreamVerification wasMade(final Matcher<Integer> countMatcher) {
        return new DownstreamVerification() {
            @Override
            public void performOnStage(ActorOnStage actorOnStage) {
                WireMockContext verificationContext = new WireMockContext(actorOnStage);
                int count = verificationContext.count(ExtendedRequestPatternBuilder.this);
                if (!countMatcher.matches(count)) {
                    StringDescription description = new StringDescription();
                    countMatcher.describeMismatch(count, description);
                    throw new AssertionError(description.toString());
                }
            }
        };
    }

    public void buildWithin(EndpointPropertyResolver verificationContext) {
        if (urlPattern == null) {
            urlPattern = calculateUrlPattern(verificationContext);
        }
        Reflection.setValue(this, "url", urlPattern);

    }

    public DownstreamStub to(ResponseStrategy responseStrategy) {
        return will(responseStrategy);
    }

    public void toAnyKnownExternalService(boolean b) {
        this.toAllKnownExternalServices = b;
    }

    public ExtendedRequestPatternBuilder withRequestBody(StringValuePattern... bodyPattern) {
        for (StringValuePattern stringValuePattern : bodyPattern) {
            withRequestBody(stringValuePattern);
        }
        return this;

    }
}
