package com.sbg.bdd.serenity.cucumber

import com.github.tomakehurst.wiremock.common.Json
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedExchange
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedRequest
import com.sbg.bdd.wiremock.scoped.admin.model.RecordedResponse
import net.serenitybdd.cucumber.adaptor.CucumberJsonAdaptor
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import java.nio.charset.Charset

class WhenImportingPreviouslyLoggedExchanges extends Specification {
    //TODO do this by programmatically injecting the Base64 encoded Exchanges
    def 'it should reflect the entire exchange hierarchy against the correct step'() {
        given:
        def adaptor = new CucumberJsonAdaptor()
        def file = new File(Thread.currentThread().contextClassLoader.getResource('json/cucumber.json').file)
        def jsonReport = FileUtils.readFileToString(file, Charset.defaultCharset())
        def exchangesJson = Json.write(buildExchanges())
        jsonReport = jsonReport.replace('${data}', Base64.encoder.encodeToString(exchangesJson.getBytes()))
        def fileToRead = new File(file.getParentFile(), 'cucumber_with_exchanges.json')
        FileUtils.write(fileToRead,jsonReport)
        when:
        def outcomes = adaptor.loadOutcomesFrom(fileToRead)
        then:
        outcomes.size() == 1
        outcomes[0].testSteps.size() == 3
        outcomes[0].testSteps[0].children.size() == 1
        outcomes[0].testSteps[1].children.size() == 1

        def givenStep = outcomes[0].testSteps[1].children[0]
        givenStep.restQuery != null
        givenStep.restQuery.path.endsWith('/resource?name=John')
        givenStep.children.size() == 1

        givenStep.children[0].restQuery != null
        givenStep.children[0].restQuery.path.endsWith('/HelloWorldService')
        givenStep.children[0].restQuery.responseHeaders.contains("HTTP_INTERNAL_ERROR")

        outcomes[0].testSteps[2].children.size() == 1
    }

    private List<RecordedExchange> buildExchanges() {
        def exchanges = buildExchanges(0, 'http://some.host/resource?name=John')
        exchanges[0].nestedExchanges = buildExchanges(1,'/HelloWorldService')
        return exchanges
    }

    private ArrayList<RecordedExchange> buildExchanges(int level, String url) {
        List<RecordedExchange> result = new ArrayList<>()
        def request = new RecordedRequest()
        request.method = RequestMethod.POST
        request.base64Body = Base64.encoder.encodeToString(('{"level":' + level +'}').bytes);
        request.headers = new HttpHeaders(new HttpHeader('Content-type','application/json'))
        request.date = new Date(System.currentTimeMillis())
        request.path = url
        request.absoluteUrl = url
        def exchange = new RecordedExchange(request)
        def response = new RecordedResponse()
        response.date = new Date(request.getDate().getTime() + 10000)
        response.headers = new HttpHeaders(new HttpHeader('Content-type','application/xml'))
        response.base64Body = Base64.encoder.encodeToString(('<level>' + level +'</level>').bytes);
        response.status=HttpURLConnection.HTTP_INTERNAL_ERROR
        exchange.response = response
        result.add(exchange)
        return result
    }
}
