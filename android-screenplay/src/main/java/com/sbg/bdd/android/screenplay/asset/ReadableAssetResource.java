package com.sbg.bdd.android.screenplay.asset;

import com.sbg.bdd.resource.ReadableResource;
import com.sbg.bdd.resource.WritableResource;
import com.sbg.bdd.resource.file.ReadableFileResource;

import java.io.IOException;
import java.io.InputStream;


public class ReadableAssetResource extends AssetResource implements ReadableResource {
    public ReadableAssetResource(AssetResourceContainer container, String name) {
        super(container, name);
    }

    @Override
    public byte[] read() {
        try {
            InputStream inputStream = getRoot().getContext().getAssets().open(getPath());
            return ReadableFileResource.toBytes(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public WritableResource asWritable() {
        throw new IllegalStateException("Cannot write to an Android Asset resource");
    }

    @Override
    public boolean canWrite() {
        return false;
    }
}
