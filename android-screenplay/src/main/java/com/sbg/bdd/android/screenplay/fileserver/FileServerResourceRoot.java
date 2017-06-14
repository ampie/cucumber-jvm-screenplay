package com.sbg.bdd.android.screenplay.fileserver;

import com.sbg.bdd.resource.ResourceRoot;

public class FileServerResourceRoot extends FileServerResourceContainer implements ResourceRoot {
    private final FileClient client;

    public FileServerResourceRoot(FileClient client) {
        super(null, null);
        this.client = client;
    }

    @Override
    public String getName() {
        return getRootName();
    }

    public FileClient getClient() {
        return client;
    }

    @Override
    public FileServerResourceRoot getRoot() {
        return this;
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public String getRootName() {
        return client.host + ":" + client.port;
    }
}
