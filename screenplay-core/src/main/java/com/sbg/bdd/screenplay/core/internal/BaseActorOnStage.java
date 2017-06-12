package com.sbg.bdd.screenplay.core.internal;

import com.sbg.bdd.screenplay.core.*;
import com.sbg.bdd.screenplay.core.annotations.ActorInvolvement;
import com.sbg.bdd.screenplay.core.annotations.Step;
import com.sbg.bdd.screenplay.core.events.ActorEvent;
import com.sbg.bdd.screenplay.core.util.NameConverter;

import java.util.ArrayList;
import java.util.List;

public class BaseActorOnStage implements ActorOnStage {
    protected final String id;
    protected Scene scene;
    protected Actor actor;
    protected SimpleMemory memory = new SimpleMemory();
    protected List<DownstreamVerification> downstreamExpectations = new ArrayList<>();
    protected boolean active = false;

    public BaseActorOnStage(Scene scene, Actor actor) {
        this.scene = scene;
        this.actor = actor;
        ((BaseActor)this.actor).onStage(this);
        this.id = NameConverter.filesystemSafe(actor.getName());

    }


    public static boolean isEverybody(ActorOnStage a) {
        return a.getId().equals(Actor.EVERYBODY);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void allow(final DownstreamStub... downstreamStubs) {
        ((BaseActor) actor).performSteps(new ScreenplayStepMethodInfo[]{new ScreenplayStepMethodInfo(((BaseActor) actor).getParentStepPath(), "allow", this, new Object() {
            String keyword = actor.getPrecedingKeyword();
            String name = actor.getName();

            @Step("#keyword #name, allow ")
            public void allow(ActorOnStage me) {
                actor.performSteps("performOnStage", BaseActorOnStage.this, downstreamStubs);
            }
        })});
    }

    @Override
    public void verifyThat(final DownstreamVerification... downstreamVerifications) {
        ((BaseActor) actor).performSteps(new ScreenplayStepMethodInfo[]{new ScreenplayStepMethodInfo(((BaseActor) actor).getParentStepPath(), "verifyThat", this, new Object() {
            String keyword = actor.getPrecedingKeyword();
            String name = actor.getName();

            @Step("#keyword #name, verify that ")
            public void verifyThat(ActorOnStage me) {
                actor.performSteps("performOnStage", BaseActorOnStage.this, downstreamVerifications);
            }
        })});
    }

    @Override
    public void expect(DownstreamExpectation... downstreamExpectations) {
        DownstreamStub[] stubs = new DownstreamStub[downstreamExpectations.length];
        for (int i = 0; i < downstreamExpectations.length; i++) {
            stubs[i] = downstreamExpectations[i].getStub();
            this.downstreamExpectations.add(downstreamExpectations[i].getVerification());
        }
        allow(stubs);
    }


    @Override
    public void evaluateExpectations() {
        if(downstreamExpectations.size()>0) {
            verifyThat(downstreamExpectations.toArray(new DownstreamVerification[0]));
        }
    }

    @Override
    public Actor getActor() {
        return actor;
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public <T> T recallImmediately(String variableName) {
        return memory.recall(variableName);
    }

    @Override
    public void remember(String name, Object value) {
        memory.remember(name, value);
    }

    @Override
    public void remember(Object value) {
        memory.remember(value);
    }

    @Override
    public void forget(String name) {
        memory.forget(name);
    }

    @Override
    public <T> T recall(String name) {
        T localValue = memory.recall(name);
        if (localValue == null) {
            return getScene().recall(name);
        }
        return localValue;
    }

    @Override
    public <T> T recall(Class<T> clzz) {
        return recall(clzz.getName());
    }

    public boolean isActive() {
        return this.active;
    }

    public void enterSpotlight() {
        if (isActive()) {
            getScene().getPerformance().getEventBus().broadcast(new ActorEvent(this, ActorInvolvement.INTO_SPOTLIGHT));
        }
    }

    public void exitSpotlight() {
        if (isActive()) {
            getScene().getPerformance().getEventBus().broadcast(new ActorEvent(this, ActorInvolvement.OUT_OF_SPOTLIGHT));
        }
    }

    public void enterStage() {
        if (!isActive()) {
            getScene().getPerformance().getEventBus().broadcast(new ActorEvent(this, ActorInvolvement.BEFORE_ENTER_STAGE));
            enterStageWithoutEvents();
            getScene().getPerformance().getEventBus().broadcast(new ActorEvent(this, ActorInvolvement.AFTER_ENTER_STAGE));
        }
    }

    public void exitStage() {
        if (isActive()) {
            getScene().getPerformance().getEventBus().broadcast(new ActorEvent(this, ActorInvolvement.BEFORE_EXIT_STAGE));
            exitStageWithoutEvents();
            getScene().getPerformance().getEventBus().broadcast(new ActorEvent(this, ActorInvolvement.AFTER_EXIT_STAGE));
        }
    }

    protected void exitStageWithoutEvents() {
        memory.clear();
        this.active = false;
    }

    protected void enterStageWithoutEvents() {
        this.active = true;
    }
}
