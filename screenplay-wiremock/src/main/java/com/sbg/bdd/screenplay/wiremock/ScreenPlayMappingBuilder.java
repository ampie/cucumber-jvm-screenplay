package com.sbg.bdd.screenplay.wiremock;

import com.sbg.bdd.screenplay.core.ActorOnStage;
import com.sbg.bdd.screenplay.core.DownstreamStub;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedMappingBuilder;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedRequestPatternBuilder;
import com.sbg.bdd.wiremock.scoped.recording.builders.ExtendedResponseDefinitionBuilder;
import com.sbg.bdd.wiremock.scoped.recording.builders.RecordingSpecification;


public class ScreenPlayMappingBuilder extends ExtendedMappingBuilder<ScreenPlayMappingBuilder> implements DownstreamStub {
    public ScreenPlayMappingBuilder(ExtendedRequestPatternBuilder requestPatternBuilder,
                                    ExtendedResponseDefinitionBuilder responseDefinitionBuilder,
                                    RecordingSpecification recordingSpecification) {
        super(requestPatternBuilder, responseDefinitionBuilder, recordingSpecification);
    }

    public ScreenPlayMappingBuilder(ExtendedRequestPatternBuilder requestPatternBuilder) {
        super(requestPatternBuilder);
    }

    @Override
    public void performOnStage(ActorOnStage actorOnStage) {
        super.applyTo(new WireMockScopeContext(actorOnStage));
    }
}
