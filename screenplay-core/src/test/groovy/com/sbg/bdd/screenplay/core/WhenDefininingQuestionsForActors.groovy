package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.annotations.StepEventType
import com.sbg.bdd.screenplay.core.internal.BaseActor

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.*
import static org.hamcrest.Matchers.*

class WhenDefininingQuestionsForActors extends WithinBasePerformance {

    def 'the question should be logged with the appropriate keywords and name '() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        OnStage.raiseTheCurtain("scene1")

        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        def theAnswer = new TheAnswerOfFive()
        when:
        then(actorNamed('John')).should(seeThat(theAnswer,is(equalTo(5))))
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 4
        events[0].info.keyword == 'should'
        events[0].info.name =='Then John should '
        events[1].info.keyword == 'evaluateFor'
        events[1].info.name =='see that the answer is <5>'
    }

    def 'for questions with multiple matchers, each matcher should be logged with the appropriate keywords and names'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        OnStage.raiseTheCurtain("scene1")

        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        def theAnswer = new TheAnswerOfFive()
        when:
        then(actorNamed('John')).should(seeThat(theAnswer,is(equalTo(5)),is(lessThan(6)),is(greaterThan(4))))
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 8
        events[0].info.keyword == 'should'
        events[0].info.name =='Then John should '
        events[1].info.keyword == 'evaluateFor'
        events[1].info.name =='see that the answer is <5>'
        events[3].info.name =='see that the answer is a value less than <6>'
        events[5].info.name =='see that the answer is a value greater than <4>'
    }
    def 'boolean questions should be logged with the appropriate keywords and name'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        OnStage.raiseTheCurtain("scene1")

        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        def iDoExist = new IDoExist()
        when:
        then(actorNamed('John')).should(seeThat(iDoExist))
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 4
        events[0].info.keyword == 'should'
        events[0].info.name =='Then John should '
        events[1].info.keyword == 'evaluateFor'
        events[1].info.name =='see that I do exist'
    }
    def 'failing questions should throw their associated diagnostic error'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        OnStage.raiseTheCurtain("scene1")

        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        def error = null
        when:
        try {
            then(actorNamed('John')).should(seeThat(new DiagnosticQuestionThatFails()))
        } catch (MyDiagnosticError e) {
            error=e
        }
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 4
        events[0].info.keyword == 'should'
        events[0].info.name =='Then John should '
        events[1].info.keyword == 'evaluateFor'
        events[1].info.name =='see that a MyDiagnosticError is thrown'
        events[2].error == error
        events[2].type == StepEventType.ASSERTION_FAILED
    }
    def 'questions with acronynms should reflect the acronym in uppercase'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        OnStage.raiseTheCurtain("scene1")

        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
            then(actorNamed('John')).should(seeThat(new QuestionTBDAcronym()))
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 4
        events[0].info.keyword == 'should'
        events[0].info.name =='Then John should '
        events[1].info.keyword == 'evaluateFor'
        events[1].info.name =='see that question TBD acronym'
    }
}
