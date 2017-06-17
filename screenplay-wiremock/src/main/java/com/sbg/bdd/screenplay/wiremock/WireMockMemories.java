package com.sbg.bdd.screenplay.wiremock;

import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.resource.ResourceRoot;
import com.sbg.bdd.screenplay.core.Memory;
import com.sbg.bdd.screenplay.core.actors.Performance;
import com.sbg.bdd.screenplay.core.util.ScreenplayMemories;
import com.sbg.bdd.wiremock.scoped.ScopedHttpAdminClient;
import com.sbg.bdd.wiremock.scoped.admin.ScopedAdmin;
import com.sbg.bdd.wiremock.scoped.admin.model.JournalMode;
import com.sbg.bdd.wiremock.scoped.recording.RecordingWireMockClient;
import com.sbg.bdd.wiremock.scoped.recording.WireMockContext;
import com.sbg.bdd.wiremock.scoped.recording.endpointconfig.EndpointConfigRegistry;
import com.sbg.bdd.wiremock.scoped.recording.endpointconfig.RemoteEndPointConfigRegistry;
import okhttp3.OkHttpClient;

import java.net.MalformedURLException;
import java.net.URL;

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
    public WireMockMemories toUseTheEndpointConfigRegistry(EndpointConfigRegistry endpointConfigRegistry) {
        memory.remember(WireMockScreenplayContext.ENDPOINT_CONFIG_REGISTRY, endpointConfigRegistry);
        return this;
    }
    public WireMockMemories toPointTo(String baseUrl) {
        memory.remember(WireMockScreenplayContext.BASE_URL_OF_SERVICE_UNDER_TEST, baseUrl);
        toUseTheEndpointConfigRegistry(new RemoteEndPointConfigRegistry(new OkHttpClient(), baseUrl));
        return this;
    }

    public WireMockMemories toUseTheJournalAt(ResourceContainer root) {
        memory.remember(WireMockScreenplayContext.JOURNAL_RESOURCE_ROOT, root);
        return this;
    }
    public WireMockMemories theRunId(Integer runId) {
        memory.remember(WireMockScreenplayContext.RUN_ID, runId);
        return this;
    }

    public WireMockMemories toUseWireMockAt(String path) {
        try {
            URL baseUrl = new URL(path);
            toUseWireMock(new ScopedHttpAdminClient(baseUrl.getHost(), baseUrl.getPort(), baseUrl.getPath()));
            return this;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
    public WireMockMemories withPublicAddress(String address) {
        memory.remember(WireMockScreenplayContext.WIRE_MOCK_PUBLIC_ADDRESS, address);
        return this;
    }


    public WireMockMemories toUseWireMock(ScopedAdmin admin) {
        memory.remember(WireMockScreenplayContext.WIRE_MOCK_ADMIN, admin);
        memory.remember(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT, new RecordingWireMockClient(admin));
        return this;
    }

    public WireMockMemories toFailUnmappedEndpoints() {
        memory.remember(WireMockScreenplayContext.PROXY_UNMAPPED_ENDPOINTS, false);
        return this;
    }

    public RecordingWireMockClient theWireMockClient() {
        return memory.recall(WireMockScreenplayContext.RECORDING_WIRE_MOCK_CLIENT);
    }
    public ScopedAdmin theWireMockAdmin() {
        return memory.recall(WireMockScreenplayContext.WIRE_MOCK_ADMIN);
    }
    public EndpointConfigRegistry theEndointConfigRegistry() {
        return memory.recall(WireMockScreenplayContext.ENDPOINT_CONFIG_REGISTRY);
    }
    public String theBaseUrlOfTheServiceUnderTest() {
        return memory.recall(WireMockScreenplayContext.BASE_URL_OF_SERVICE_UNDER_TEST);
    }
    public String thePublicAddressOfWireMock() {
        return memory.recall(WireMockScreenplayContext.WIRE_MOCK_PUBLIC_ADDRESS);
    }

    public JournalMode theJournalModeToUse() {
        return memory.recall(WireMockScreenplayContext.JOURNAL_MODE);
    }

    public Integer theRunId() {
            return memory.recall(WireMockScreenplayContext.RUN_ID);
    }
}
