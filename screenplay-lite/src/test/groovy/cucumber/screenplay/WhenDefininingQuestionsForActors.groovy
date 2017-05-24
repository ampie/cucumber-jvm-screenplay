package cucumber.screenplay

import cucumber.screenplay.annotations.StepEventType
import cucumber.screenplay.internal.BasePerformance
import cucumber.screenplay.actors.OnStage
import cucumber.screenplay.internal.BaseActor
import spock.lang.Specification

import static cucumber.screenplay.ScreenplayPhrases.*
import static org.hamcrest.Matchers.*

class WhenDefininingQuestionsForActors extends Specification {

    def 'the question should be logged with the appropriate keywords and name '() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = new BasePerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        def theAnswer = new TheAnswerOfFive()
        when:
        then(actorNamed('John')).should(seeThat(theAnswer,is(equalTo(5))))
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 2
        events[0].info.keyword == 'Should'
        events[0].info.name =='Then the answer should be <5>'
    }

    def 'for questions with multiple matchers, each matcher should be logged with the appropriate keywords and names'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = new BasePerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        def theAnswer = new TheAnswerOfFive()
        when:
        then(actorNamed('John')).should(seeThat(theAnswer,is(equalTo(5)),is(lessThan(6)),is(greaterThan(4))))
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 6
        events[0].info.keyword == 'Should'
        events[0].info.name =='Then the answer should be <5>'
        events[2].info.name =='Then the answer should be a value less than <6>'
        events[4].info.name =='Then the answer should be a value greater than <4>'
    }
    def 'boolean questions should be logged with the appropriate keywords and name'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = new BasePerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        def iDoExist = new IDoExist()
        when:
        then(actorNamed('John')).should(seeThat(iDoExist))
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 2
        events[0].info.keyword == 'Should'
        events[0].info.name =='Then I do exist'
    }
    def 'failing questions should throw their associated diagnostic error'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = new BasePerformance()
        OnStage.present(performanceStub)
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
        events.size() == 2
        events[0].info.keyword == 'Should'
        events[0].info.name =='Then diagnose it'
        events[1].error == error
        events[1].type == StepEventType.STEP_ASSERTION_FAILED
    }
    def 'questions with acronynms should reflect the acronym in uppercase'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = new BasePerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))
        when:
            then(actorNamed('John')).should(seeThat(new QuestionTBDAcronym()))
        then:
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 2
        events[0].info.keyword == 'Should'
        events[0].info.name =='Then question TBD acronym'
    }
}
