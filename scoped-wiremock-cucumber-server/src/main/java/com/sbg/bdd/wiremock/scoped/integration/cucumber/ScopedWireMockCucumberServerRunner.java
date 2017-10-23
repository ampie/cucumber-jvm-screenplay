package com.sbg.bdd.wiremock.scoped.integration.cucumber;

import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.wiremock.scoped.admin.ScopedAdmin;
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServer;
import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServerRunner;
import com.sbg.bdd.wiremock.scoped.server.recording.RecordingManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScopedWireMockCucumberServerRunner {
    public static void main(String[] args) {
        //HACK to get around Serenity's assumption of a URLClassLoader a.o.t. jar app loader
        String resourcesJar =Thread.currentThread().getContextClassLoader().getResource("report-resources/favicon.ico").toString();
        resourcesJar = resourcesJar.substring("jar:file:".length(), resourcesJar.lastIndexOf("jar!")+3);
        String cp = System.getProperty("java.class.path");
        if(cp==null){
            System.setProperty("java.class.path",resourcesJar);
        }else if(!cp.contains(resourcesJar)){
            System.setProperty("java.class.path",cp + File.pathSeparator + resourcesJar);
        }
        System.setProperty("http.keepAlive", "false");
        List<String> newArgs = new ArrayList(Arrays.asList(args));
        newArgs.add("--container-threads");
        newArgs.add("50");
        //TODO add this extension
        CucumberFormattingScopeListener.class.getName();
        ScopedWireMockServerRunner.main(newArgs.toArray(new String[newArgs.size()]));
        ScopedWireMockServer server = ScopedWireMockServerRunner.getWireMockServer();
        ResourceContainer inputRoot = server.getResourceRoot(ScopedAdmin.INPUT_RESOURCE_ROOT);
        if(inputRoot!=null){
            if(server.getResourceRoot(ScopedAdmin.PERSONA_RESOURCE_ROOT)==null){
                server.registerResourceRoot(ScopedAdmin.PERSONA_RESOURCE_ROOT, (ResourceContainer) inputRoot.getChild("personas"));
            }
        }
    }
}
