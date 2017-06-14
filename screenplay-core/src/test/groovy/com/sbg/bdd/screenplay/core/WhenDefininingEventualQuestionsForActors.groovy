package com.sbg.bdd.screenplay.core

import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.internal.BaseActor

import static com.sbg.bdd.screenplay.core.EventualConsequence.eventually
import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.*

class WhenDefininingEventualQuestionsForActors extends WithinBasePerformance {
    def 'eventual questions should be logged with the appropriate keywords and name'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(new StopWatchStub(9999))

        def performanceStub = buildPerformance()
        OnStage.present(performanceStub)
        performanceStub.eventBus.scanClasses(new HashSet<Class<?>>(Arrays.asList(ScreenPlayEventStore)))

        def iDied = new IDiedAfterASecond()
        def start = System.currentTimeMillis()
        when:
        then(actorNamed('John')).should(eventually(seeThat(iDied)).waitingForNoLongerThan(2000).milliseconds())
        then:
        System.currentTimeMillis() > start + 1000

        def events = ScreenPlayEventStore.getEvents();
        events.size() == 4
        events[0].info.keyword == 'should'
        events[0].info.name == 'Then John should '
        events[1].info.keyword == 'evaluateFor'
        events[1].info.name =='eventually see that I died after a second'
    }
    def 'eventual questions that expire should complain with the appropriate exception and message'() {
        given:
        ScreenPlayEventStore.events.clear()
        BaseActor.useStopWatch(null)

        def performanceStub = buildPerformance()
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
        events.size() == 4
        events[0].info.keyword == 'should'
        events[0].info.name =='Then John should '
        events[1].info.keyword == 'evaluateFor'
        events[1].info.name =='eventually see that I died after a second'
        events[2].error == error
        events[2].duration > 100 * 1000000
        events[2].duration < 200 * 1000000
        error.message.startsWith('arrrrgh!') == true
    }
}
