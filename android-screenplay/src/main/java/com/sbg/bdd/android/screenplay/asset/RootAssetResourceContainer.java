package com.sbg.bdd.android.screenplay.asset;

import android.content.Context;
import com.sbg.bdd.resource.ResourceRoot;


public class RootAssetResourceContainer extends AssetResourceContainer implements ResourceRoot{
    private final Context context;

    public RootAssetResourceContainer(Context context) {
        super(null, "assets");
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

    @Override
    public String getRootName() {
        return "assets";
    }
}
