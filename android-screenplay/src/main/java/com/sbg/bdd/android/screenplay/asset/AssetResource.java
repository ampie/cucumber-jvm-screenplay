package com.sbg.bdd.android.screenplay.asset;

import android.content.res.AssetManager;
import com.sbg.bdd.resource.Resource;


public abstract class AssetResource implements Resource {
    private final String name;
    private final AssetResourceContainer container;

    public AssetResource(AssetResourceContainer container, String name) {
        this.container = container;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getPath() {
        if (getContainer() instanceof RootAssetResourceContainer) {
            return getName();
        } else {
            return getContainer().getPath() + "/" + getName();
        }
    }

    protected AssetManager getAssets() {
        return getRoot().getContext().getAssets();
    }

    @Override
    public RootAssetResourceContainer getRoot() {
        return getContainer().getRoot();
    }

    @Override
    public AssetResourceContainer getContainer() {
        return container;
    }


}
