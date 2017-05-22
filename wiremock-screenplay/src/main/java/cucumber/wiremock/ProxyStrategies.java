package cucumber.wiremock;

import static cucumber.wiremock.ResponseStrategies.aResponse;


public abstract class ProxyStrategies {
    public static class ProxyMappingBuilder {
        String baseUrl;
        int segments;
        String action;
        String which;
        private boolean targetTheServiceUnderTest;

        public ProxyMappingBuilder(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        public ProxyMappingBuilder ignoring(){
            action="ignore";
            return this;
        }
        public ProxyMappingBuilder using(){
            action="use";
            return this;
        }
        public ProxyMappingBuilder theLast(int number){
            which="trailing";
            segments=number;
            return this;
        }
        public ProxyMappingBuilder theFirst(int number){
            which="leading";
            segments=number;
            return this;
        }
        public ProxyMappingBuilder theServiceUnderTest(){
            targetTheServiceUnderTest=true;
            return this;
        }
        public ResponseStrategy segments(){
            return target(baseUrl, segments, action, which);
        }

    }
    public static ProxyMappingBuilder target(){
        return new ProxyMappingBuilder(null);
    }
    public static ProxyMappingBuilder target(final String baseUrl){
        return new ProxyMappingBuilder(baseUrl);
    }

    private static ResponseStrategy target(final String baseUrl, final int segments, final String action, final String which) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext context) throws Exception {
                builder.getRequestPatternBuilder().changeUrlToPattern();
                builder.atPriority(context.calculatePriority(4));
                String baseUrlToUse=baseUrl == null?context.getBaseUrlOfServiceUnderTest():baseUrl;
                return aResponse().proxiedFrom(baseUrlToUse).withTransformers("ProxyUrlTransformer")
                        .withTransformerParameter("numberOfSegments", segments)
                        .withTransformerParameter("action", action)
                        .withTransformerParameter("which", which);
            }
        };
    }

    public static ResponseStrategy proxyTo(final String baseUrl) {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext context) throws Exception {
                builder.getRequestPatternBuilder().changeUrlToPattern();
                builder.atPriority(context.calculatePriority(5));
                return aResponse().proxiedFrom(baseUrl);
            }
        };
    }


    public static ResponseStrategy beIntercepted() {
        return new ResponseStrategy() {
            public ExtendedResponseDefinitionBuilder applyTo(ExtendedMappingBuilder builder, WireMockContext context) throws Exception {
                builder.atPriority(context.calculatePriority(5));
                builder.getRequestPatternBuilder().changeUrlToPattern();
                return aResponse().interceptedFromSource();
            }
        };

    }


}
