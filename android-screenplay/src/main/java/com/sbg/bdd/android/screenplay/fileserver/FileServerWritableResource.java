package com.sbg.bdd.android.screenplay.fileserver;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.WritableResource;

public class FileServerWritableResource extends FileServerResource implements WritableResource {
    protected FileServerWritableResource(FileServerResourceContainer container, String name) {
        super(container, name);
    }

    @Override
    public void write(byte[] data) {
        getRoot().getClient().write(getPath(), data);
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public ReadableResource asReadable() {
        return new FileServerReadableResource(getContainer(), getName(), true);
    }
}
