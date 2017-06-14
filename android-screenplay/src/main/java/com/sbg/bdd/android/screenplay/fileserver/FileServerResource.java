package com.sbg.bdd.android.screenplay.fileserver;

import com.sbg.bdd.resource.Resource;

public abstract class FileServerResource implements Resource {
    private String name;
    private FileServerResourceContainer container;

    protected FileServerResource(FileServerResourceContainer container, String name) {
        this.container = container;
        this.name = name;
    }

    @Override
    public String getPath() {
        if (getContainer() instanceof FileServerResourceRoot) {
            return getName();
        } else {
            return getContainer().getPath() + "/" + getName();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FileServerResourceRoot getRoot() {
        return getContainer().getRoot();
    }

    @Override
    public FileServerResourceContainer getContainer() {
        return container;
    }
}
