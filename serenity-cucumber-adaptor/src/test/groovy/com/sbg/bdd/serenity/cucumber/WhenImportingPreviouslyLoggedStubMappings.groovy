package com.sbg.bdd.serenity.cucumber

import com.github.tomakehurst.wiremock.common.Json
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.sbg.bdd.wiremock.scoped.client.WireMockContext
import com.sbg.bdd.wiremock.scoped.client.builders.ExtendedMappingBuilder
import net.serenitybdd.cucumber.adaptor.CucumberJsonAdaptor
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import java.nio.charset.Charset

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.http.RequestMethod.*
import static com.sbg.bdd.wiremock.scoped.client.strategies.RequestStrategies.*
import static com.sbg.bdd.wiremock.scoped.client.strategies.ResponseBodyStrategies.*
import static java.util.Arrays.asList

class WhenImportingPreviouslyLoggedStubMappings extends Specification {
    def 'it should reflect a single stubMapping as the restQuery against the step it occurred in'() {
        given: 'a single stubMapping under the single child step under the second step '
        def adaptor = new CucumberJsonAdaptor()
        def file = new File(Thread.currentThread().contextClassLoader.getResource('json/cucumber.json').file)
        def jsonReport = FileUtils.readFileToString(file, Charset.defaultCharset())
        def mappingsJson = Json.write(buildOneStubMapping())
        jsonReport = jsonReport.replace('${data}', Base64.encoder.encodeToString(mappingsJson.getBytes()))
        def fileToRead = new File(file.getParentFile(), 'cucumber_with_mappings.json')
        FileUtils.write(fileToRead,jsonReport)
        when:'the cucumber adaptor loads the outcomes from the cucumber json file'
        def outcomes = adaptor.loadOutcomesFrom(fileToRead)
        then: 'the mapping should be represented as the rest query of the single child step of the second step'
        outcomes.size() == 1
        outcomes[0].testSteps.size() == 3
        outcomes[0].testSteps[0].children.size() == 1
        outcomes[0].testSteps[1].children.size() == 1

        def givenStep = outcomes[0].testSteps[1].children[0]
        givenStep.restQuery != null
        givenStep.children.size() == 0
        givenStep.restQuery.path.endsWith('/resource')
        givenStep.restQuery.parameterMap.get()['name'] == 'John'
        givenStep.restQuery.responseBody == 'my body is my temple'
        givenStep.children.size() == 0

        outcomes[0].testSteps[2].children.size() == 1
    }

    private List<StubMapping> buildOneStubMapping() {
        def builder = a(GET).to('http://localhost/resource').will(returnTheBody('my body is my temple','text/plain'))
        builder.withQueryParam('name', equalTo('John'))
        applyStrategy(builder)
        return asList(builder.build())
    }
    def 'it should reflect multiple stubMappings as childsteps with restQueries against the step it occurred in'() {
        given: 'multiple stubMappings under the single child step under the second step '
        def adaptor = new CucumberJsonAdaptor()
        def file = new File(Thread.currentThread().contextClassLoader.getResource('json/cucumber.json').file)
        def jsonReport = FileUtils.readFileToString(file, Charset.defaultCharset())
        def mappingsJson = Json.write(buildTwoStubMappings())
        jsonReport = jsonReport.replace('${data}', Base64.encoder.encodeToString(mappingsJson.getBytes()))
        def fileToRead = new File(file.getParentFile(), 'cucumber_with_mappings.json')
        FileUtils.write(fileToRead,jsonReport)
        when:'the cucumber adaptor loads the outcomes from the cucumber json file'
        def outcomes = adaptor.loadOutcomesFrom(fileToRead)
        then:'the mapping should be represented as the child steps with rest queries under the single child step of the second step'
        outcomes.size() == 1
        outcomes[0].testSteps.size() == 3
        outcomes[0].testSteps[0].children.size() == 1
        outcomes[0].testSteps[1].children.size() == 1

        def givenStep = outcomes[0].testSteps[1].children[0]
        givenStep.restQuery == null
        givenStep.children.size() == 2
        givenStep.children[0].restQuery != null
        givenStep.children[0].restQuery.path.endsWith('/resource')
        givenStep.children[0].restQuery.parameterMap.get()['name'] == 'John'
        givenStep.children[0].restQuery.responseBody == 'my body is my temple'

        outcomes[0].testSteps[2].children.size() == 1
    }

    private List<StubMapping> buildTwoStubMappings() {
        def builder = a(GET).to('/resource').will(returnTheBody('my body is my temple','text/plain'))
        builder.withQueryParam('name', equalTo('John'))
        applyStrategy(builder)
        return asList(builder.build(),builder.build())
    }

    private applyStrategy(ExtendedMappingBuilder builder) {
        builder.applyTo(Mock(WireMockContext) {
            getCorrelationPath() >> 'localhost/1808/asdf/0'
        })
    }
}
