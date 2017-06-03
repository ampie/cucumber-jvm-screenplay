package com.sbg.bdd.android.screenplay.fileserver;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.WritableResource;

public class FileServerReadableResource extends FileServerResource implements ReadableResource {
    private boolean writable;

    protected FileServerReadableResource(FileServerResourceContainer container, String name, boolean writable) {
        super(container, name);
        this.writable = writable;
    }

    @Override
    public byte[] read() {
        return getRoot().getClient().read(getPath());
    }

    @Override
    public FileServerWritableResource asWritable() {
        return new FileServerWritableResource(getContainer(), getName());
    }

    @Override
    public boolean canWrite() {
        return writable;
    }
}
