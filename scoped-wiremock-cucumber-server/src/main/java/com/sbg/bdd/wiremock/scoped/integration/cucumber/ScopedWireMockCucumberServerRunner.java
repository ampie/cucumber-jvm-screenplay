package com.sbg.bdd.wiremock.scoped.integration.cucumber;

import com.sbg.bdd.wiremock.scoped.server.ScopedWireMockServerRunner;

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
        List<String> newArgs = new ArrayList(Arrays.asList(args));
        //TODO add this extension
        CucumberFormattingScopeListener.class.getName();
        ScopedWireMockServerRunner.main(newArgs.toArray(new String[newArgs.size()]));
    }
}
