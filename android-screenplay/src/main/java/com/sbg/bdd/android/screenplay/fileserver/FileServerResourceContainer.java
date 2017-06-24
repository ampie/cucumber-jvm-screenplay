package com.sbg.bdd.android.screenplay.fileserver;

import com.sbg.bdd.resource.Resource;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.resource.ResourceFilter;
import com.sbg.bdd.resource.ResourceSupport;
import com.sbg.bdd.resource.file.DirectoryResource;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class FileServerResourceContainer extends FileServerResource implements ResourceContainer {
    private Map<String, FileServerResource> children;

    public FileServerResourceContainer(FileServerResourceContainer container, String name) {
        super(container, name);
    }

    @Override
    public FileServerResource[] list() {
        return getChildren().values().toArray(new FileServerResource[getChildren().size()]);
    }

    private Map<String, FileServerResource> getChildren() {
        if (children == null) {
            List<String> list = getClient().list(getPath());
            children = new TreeMap<>();
            for (int i = 0; i < list.size(); i++) {
                putChildNamed(list.get(i));
            }
        }
        return children;
    }

    private void putChildNamed(String s) {
        String[] split = s.split("\\|");
        if (split[1].equals("DIR")) {
            children.put(split[0], new FileServerResourceContainer(this, split[0]));
        } else {
            children.put(split[0], new FileServerReadableResource(this, split[0], split[1].equals("FRW")));
        }
    }

    private FileClient getClient() {
        return getRoot().getClient();
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
    public FileServerResourceContainer resolvePotentialContainer(String... segments) {
        String[] flattened = ResourceSupport.flatten(segments);
        FileServerResource previous = resolvePotential(flattened, flattened.length);
        if (previous instanceof FileServerResourceContainer) {
            return (FileServerResourceContainer) previous;
        } else {
            return null;
        }
    }

    @Override
    public FileServerWritableResource resolvePotential(String... segments) {
        String[] flattened = ResourceSupport.flatten(segments);
        FileServerResource previous = resolvePotential(flattened, flattened.length - 1);
        if (previous instanceof FileServerWritableResource) {
            return (FileServerWritableResource) previous;
        } else if (previous instanceof FileServerReadableResource) {
            return ((FileServerReadableResource) previous).asWritable();
        } else {
            return null;
        }
    }

    private FileServerResource resolvePotential(String[] flattened, int lastDirectoryIndex) {
        FileServerResource previous = this;
        for (int i = 0; i < flattened.length; i++) {
            if (previous instanceof FileServerResourceContainer) {
                FileServerResourceContainer previousContainer = (FileServerResourceContainer) previous;
                FileServerResource child = previousContainer.getChild(flattened[i]);
                if (child == null) {
                    if (i < lastDirectoryIndex) {
                        child = new FileServerResourceContainer(previousContainer, flattened[i]);
                    } else {
                        child = new FileServerWritableResource(previousContainer, flattened[i]);
                    }
                }
                previous = child;
            }
        }
        return previous;
    }

    @Override
    public boolean fallsWithin(String path) {
        return true;
    }

    @Override
    public FileServerResource getChild(String segment) {
        return getChildren().get(segment);
    }
}
