package com.sbg.bdd.screenplay.cucumber.junit;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.ResourceContainer;
import cucumber.runtime.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GenericResourceCucumberResource implements Resource {
    private com.sbg.bdd.resource.Resource delegate;
    private ResourceContainer cucumberResourceRoot;

    public GenericResourceCucumberResource(ResourceContainer cucumberResourceRoot, com.sbg.bdd.resource.Resource resource) {
        delegate = resource;
        this.cucumberResourceRoot = cucumberResourceRoot;
    }

    @Override
    public String getPath() {
        String path = delegate.getPath().substring(cucumberResourceRoot.getPath().length());
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    @Override
    public String getAbsolutePath() {
        return getPath();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(((ReadableResource) delegate).read());
    }

    @Override
    public String getClassName(String extension) {
        return getPath().substring(0, getPath().length() - extension.length()).replace('/', '.');
    }
}
