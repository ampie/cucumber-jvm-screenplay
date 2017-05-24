package cucumber.screenplay

import cucumber.screenplay.internal.BasePerformance
import cucumber.screenplay.actors.OnStage
import cucumber.screenplay.internal.BaseActor
import spock.lang.Specification

import static cucumber.screenplay.EventualConsequence.eventually
import static cucumber.screenplay.ScreenplayPhrases.*

class WhenDefininingEventualQuestionsForActors extends Specification {
    def 'eventual questions should be logged with the appropriate keywords and name'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = new BasePerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))

        def iDied = new IDiedAfterASecond()
        def start = System.currentTimeMillis()
        when:
        then(actorNamed('John')).should(eventually(seeThat(iDied)).waitingForNoLongerThan(2000).milliseconds())
        then:
        System.currentTimeMillis() > start + 1000

        def events = ScreenPlayEventStore.getEvents();
        events.size() == 2
        events[0].info.keyword == 'Should'
        events[0].info.name =='Then I died after a second'
    }
    def 'eventual questions that expire should complain with the appropriate exception and message'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(null)

        def performanceStub = new BasePerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))

        def iDied = new IDiedAfterASecond()
        def start = System.currentTimeMillis()
        def error = null
        when:
            try {
                then(actorNamed('John')).should(eventually(seeThat(iDied)).waitingForNoLongerThan(100).milliseconds().orComplainWith(IDidNotDieError, 'arrrrgh!'))
            } catch (IDidNotDieError e) {
                error =e
            }
        then:
        System.currentTimeMillis() > start + 100
        def events = ScreenPlayEventStore.getEvents();
        events.size() == 2
        events[0].info.keyword == 'Should'
        events[0].info.name =='Then I died after a second'
        events[1].error == error
        events[1].duration > 100 * 1000000
        events[1].duration < 200 * 1000000
        error.message.startsWith('arrrrgh!') == true
    }
}
