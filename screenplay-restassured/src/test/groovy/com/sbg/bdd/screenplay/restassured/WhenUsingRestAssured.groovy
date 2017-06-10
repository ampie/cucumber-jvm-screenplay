package com.sbg.bdd.screenplay.restassured

import com.sbg.bdd.resource.file.RootDirectoryResource
import com.sbg.bdd.screenplay.core.actors.OnStage
import com.sbg.bdd.screenplay.core.internal.BasePerformance
import com.sbg.bdd.screenplay.wiremock.WireMockScreenplayContext
import com.sbg.bdd.screenplay.wiremock.listeners.ScopeManagementListener
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer
import com.sbg.bdd.wiremock.scoped.integration.BaseDependencyInjectorAdaptor
import com.sbg.bdd.wiremock.scoped.integration.BaseWireMockCorrelationState
import com.sbg.bdd.wiremock.scoped.integration.DependencyInjectionAdaptorFactory
import com.sbg.bdd.wiremock.scoped.integration.HeaderName
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient
import io.restassured.RestAssured
import io.restassured.filter.FilterContext
import io.restassured.http.Header
import io.restassured.http.Headers
import io.restassured.response.Response
import spock.lang.Specification

import static com.sbg.bdd.screenplay.core.ScreenplayPhrases.actorNamed

abstract class WhenUsingRestAssured extends Specification {
    public ScopedWireMockServer initWireMockAndBasePerformance() {
        BaseDependencyInjectorAdaptor.CURRENT_CORRELATION_STATE = new BaseWireMockCorrelationState()
        DependencyInjectionAdaptorFactory.useAdapter(new BaseDependencyInjectorAdaptor())
        def markerFile = new File(Thread.currentThread().contextClassLoader.getResource('screenplay-restassured-marker.txt').file)
        def performance = new BasePerformance("Runit", new RootDirectoryResource(markerFile.parentFile))
        performance.getEventBus().scanClasses(new HashSet<Class<?>>(Arrays.asList(ScopeManagementListener,TaskListener)))
        OnStage.present(performance)
        def server = new ScopedWireMockServer()
        server.start()
        performance.remember(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT, new RecordingWireMockClient(server))
        server
    }

}
