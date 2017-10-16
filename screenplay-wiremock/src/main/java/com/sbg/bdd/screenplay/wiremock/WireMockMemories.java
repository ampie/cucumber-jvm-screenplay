package com.sbg.bdd.screenplay.wiremock;

import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.screenplay.core.Memory;
import com.sbg.bdd.screenplay.core.util.ScreenplayMemories;
import com.sbg.bdd.wiremock.scoped.admin.ScopedAdmin;
import com.sbg.bdd.wiremock.scoped.admin.model.JournalMode;
import com.sbg.bdd.wiremock.scoped.client.ScopedHttpAdminClient;
import com.sbg.bdd.wiremock.scoped.client.ScopedWireMockClient;

import java.net.MalformedURLException;
import java.net.URL;

@Deprecated
//Rather use ScreenplayConfigurator
public class WireMockMemories extends ScreenplayMemories<WireMockMemories> {

    public WireMockMemories(Memory memory) {
        super(memory);
    }

    public static WireMockMemories rememberFor(Memory memory) {
        return new WireMockMemories(memory);
    }

    public static WireMockMemories recallFrom(Memory memory) {
        return new WireMockMemories(memory);
    }

    public WireMockMemories toProxyUnmappedEndpointsToOriginal() {
        memory.remember(WireMockScreenplayContext.PROXY_UNMAPPED_ENDPOINTS, true);
        return this;
    }

    public WireMockMemories toUseTheJournalMode(JournalMode mode) {
        memory.remember(WireMockScreenplayContext.JOURNAL_MODE, mode);
        return this;
    }

    public WireMockMemories toPointTo(String baseUrl) {
        memory.remember(WireMockScreenplayContext.BASE_URL_OF_SERVICE_UNDER_TEST, toUrl(baseUrl));
        return this;
    }

    public WireMockMemories toUseTheJournalAt(ResourceContainer root) {
        memory.remember(WireMockScreenplayContext.JOURNAL_RESOURCE_ROOT, root);
        return this;
    }

    public WireMockMemories toUseThePersonasAt(ResourceContainer root) {
        memory.remember(WireMockScreenplayContext.PERSONA_RESOURCE_ROOT, root);
        return this;
    }

    public WireMockMemories toUseWireMockAt(String path) {
        URL url = toUrl(path);
        toUseWireMock(new ScopedHttpAdminClient(url.getHost(), url.getPort(), url.getPath()));
        return this;
    }

    private URL toUrl(String path) {
        try {
            return new URL(path);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public WireMockMemories withPublicAddress(String address) {
        URL baseUrl = toUrl(address);
        memory.remember(WireMockScreenplayContext.WIRE_MOCK_PUBLIC_ADDRESS, baseUrl);
        return this;
    }

    public WireMockMemories forIntegrationScope(String integrationScope) {
        memory.remember(WireMockScreenplayContext.INTEGRATION_SCOPE, integrationScope);
        return this;
    }


    public WireMockMemories toUseWireMock(ScopedAdmin admin) {
        memory.remember(WireMockScreenplayContext.WIRE_MOCK_ADMIN, admin);
        memory.remember(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT, new ScopedWireMockClient(admin));
        return this;
    }

    public WireMockMemories toFailUnmappedEndpoints() {
        memory.remember(WireMockScreenplayContext.PROXY_UNMAPPED_ENDPOINTS, false);
        return this;
    }

    public ScopedWireMockClient theWireMockClient() {
        return memory.recall(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT);
    }

    public ScopedAdmin theWireMockAdmin() {
        return memory.recall(WireMockScreenplayContext.WIRE_MOCK_ADMIN);
    }

    public URL theBaseUrlOfTheServiceUnderTest() {
        return memory.recall(WireMockScreenplayContext.BASE_URL_OF_SERVICE_UNDER_TEST);
    }

    public URL thePublicAddressOfWireMock() {
        URL url = memory.recall(WireMockScreenplayContext.WIRE_MOCK_PUBLIC_ADDRESS);
        if (url == null) {
            ScopedWireMockClient wireMockClient = theWireMockClient();
            if (wireMockClient != null) {
                url = toUrl(wireMockClient.baseUrl());
            }
        }
        return url;
    }

    public JournalMode theJournalModeToUse() {
        return memory.recall(WireMockScreenplayContext.JOURNAL_MODE);
    }


    public String theIntegrationScope() {
        return memory.recall(WireMockScreenplayContext.INTEGRATION_SCOPE);
    }
}
