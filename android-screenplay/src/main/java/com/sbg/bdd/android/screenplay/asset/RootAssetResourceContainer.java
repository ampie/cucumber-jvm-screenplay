package com.sbg.bdd.android.screenplay.asset;

import android.content.Context;


public class RootAssetResourceContainer extends AssetResourceContainer {
    private final Context context;

    public RootAssetResourceContainer(Context context) {
        super(null, "");
        this.context = context;
    }

    @Override
    public String getPath() {
        return "";
    }

    protected String childPathFor(String segment) {
        return segment;
    }

    @Override
    public RootAssetResourceContainer getRoot() {
        return this;
    }

    public Context getContext() {
        return context;
    }
}
