package com.sbg.bdd.android.screenplay.fileserver;

public class FileServerResourceRoot extends FileServerResourceContainer {
    private final FileClient client;

    public FileServerResourceRoot(FileClient client) {
        super(null, null);
        this.client = client;
    }

    @Override
    public String getName() {
        return "";
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
}
