package com.sbg.bdd.android.screenplay.asset;

import com.sbg.bdd.resource.*;
import com.sbg.bdd.resource.file.DirectoryResource;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;


public class AssetResourceContainer extends AssetResource implements ResourceContainer {
    private Map<String, AssetResource> children;

    public AssetResourceContainer(AssetResourceContainer container, String name) {
        super(container, name);
    }

    @Override
    public Resource[] list() {
        return getChildren().values().toArray(new AssetResource[getChildren().size()]);
    }

    private Map<String, AssetResource> getChildren() {
        if (children == null) {
            String[] list = listAndCatch(getPath());
            children = new TreeMap<>();
            for (int i = 0; i < list.length; i++) {
                putChildNamed(list[i]);
            }
        }
        return children;
    }

    private void putChildNamed(String s) {
        try {
            String[] list = getAssets().list(childPathFor(s));
            if(list ==null || list.length==0){
                children.put(s, new ReadableAssetResource(this, s));
            }else {
                children.put(s, new AssetResourceContainer(this, s));
            }
        } catch (IOException e) {
            children.put(s, new ReadableAssetResource(this, s));
        }
    }

    protected String childPathFor(String segment) {
        return getPath() + "/" + segment;
    }

    private String[] listAndCatch(String path) {
        try {
            return getAssets().list(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Resource[] list(ResourceFilter filter) {
        return ResourceSupport.list(filter, getChildren(), this);
    }

    @Override
    public Resource resolveOrFail(String... segments) throws IllegalArgumentException {
        return ResourceSupport.resolveExisting(this, segments, true);
    }

    @Override
    public Resource resolveExisting(String... segments) {
        return ResourceSupport.resolveExisting(this, segments,false);
    }

    @Override
    public WritableResource resolvePotential(String... segments) {
        throw new IllegalStateException("You should never have to open a file for writing from your Android assets");
    }

    @Override
    public ResourceContainer resolvePotentialContainer(String... segments) {
        throw new IllegalStateException("You should never have to open a directory for writing from your Android assets");
    }

    @Override
    public boolean fallsWithin(String path) {
        return resolveExisting(path) != null;
    }

    @Override
    public Resource getChild(String segment) {
        return getChildren().get(segment);
    }
}
