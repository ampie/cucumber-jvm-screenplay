package com.sbg.bdd.screenplay.wiremock;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.DownstreamStub;
import com.sbg.bdd.screenplay.core.annotations.ProducesAttachment;
import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.wiremock.scoped.recording.builders.*;

import java.util.ArrayList;
import java.util.List;


public class ScreenPlayMappingBuilder extends ExtendedMappingBuilder<ScreenPlayMappingBuilder> implements DownstreamStub {
    private String requestDescription;
    private String responseDescription;
    @ProducesAttachment(mimeType = "application/json")
    private String mappings;


    public ScreenPlayMappingBuilder(ScreenPlayRequestPatternBuilder requestPatternBuilder) {
        super(requestPatternBuilder);
        this.requestDescription = requestPatternBuilder.getDescription();
    }

    @Override
    public ScreenPlayMappingBuilder to(ResponseStrategy responseStrategy) {
        this.responseDescription = responseStrategy.getDescription();
        return super.to(responseStrategy);
    }

    @Override
    @Step("#requestDescription to #responseDescription")
    public void performOnStage(ActorOnStage actorOnStage) {
        final List<StubMapping> mappings = new ArrayList<>();
        super.applyTo(new WireMockScopeContext(actorOnStage) {
            @Override
            public void register(ExtendedMappingBuilder child) {
                super.register(child);
                if (child.getResponseDefinitionBuilder() != null) {
                    mappings.add(child.build());
                }
            }
        });
        if (mappings.size() > 0) {
            this.mappings = Json.write(mappings);
        }
    }
}
