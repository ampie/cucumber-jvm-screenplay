package com.sbg.bdd.serenity.cucumber

import net.serenitybdd.cucumber.adaptor.CucumberJsonAdaptor
import spock.lang.Specification

class WhenImportingPreviouslyLoggedExchanges extends Specification {
    def 'it should reflect the entire exchange hiearchy against the correct step'(){
        given:
        def adaptor=new CucumberJsonAdaptor()
        def file = new File(Thread.currentThread().contextClassLoader.getResource('json/cucumber.json').file)
        when:
        def outcomes = adaptor.loadOutcomesFrom(file)
        then:
        outcomes.size() ==1
        outcomes[0].testSteps.size()==3
        outcomes[0].testSteps[0].children.size() == 1
        outcomes[0].testSteps[1].children.size() == 1

        def givenStep = outcomes[0].testSteps[1].children[0]
        givenStep.children.size() == 1

        givenStep.children[0].restQuery !=null
        givenStep.children[0].restQuery.path.endsWith("/resource?name=John")
        givenStep.children[0].children[0].restQuery !=null
        givenStep.children[0].children[0].restQuery.path.endsWith("/HelloWorldService")

        outcomes[0].testSteps[2].children.size() == 1
    }
}
