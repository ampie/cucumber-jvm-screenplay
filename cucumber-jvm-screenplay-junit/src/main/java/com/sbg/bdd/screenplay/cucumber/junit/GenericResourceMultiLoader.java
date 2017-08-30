package com.sbg.bdd.screenplay.cucumber.junit;

import com.sbg.bdd.resource.ResourceContainer;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;

import java.util.ArrayList;
import java.util.List;

public class GenericResourceMultiLoader extends MultiLoader {
    private ResourceContainer root;

    public GenericResourceMultiLoader(ResourceContainer root, ClassLoader cl) {
        super(cl);
        this.root = root;
    }

    @Override
    public Iterable<Resource> resources(String path, String suffix) {
        List<Resource> result = new ArrayList<>();
        //TODO maybe think of a diferent prefix:
        if (isFeatureFilePathNotOnClassPath(path, suffix)) {
            com.sbg.bdd.resource.Resource existing = root.resolveExisting(path);
            if (existing != null) {
                addRecursively(suffix, existing, result);
            }
        } else {
            for (Resource resource : super.resources(path, suffix)) {
                result.add(resource);
            }
        }
        return result;
    }

    private boolean isFeatureFilePathNotOnClassPath(String path, String suffix) {
        return !path.startsWith(CLASSPATH_SCHEME) && suffix.endsWith(".feature");
    }

    private void addRecursively(String suffix, com.sbg.bdd.resource.Resource existing, List<Resource> result) {
        if (existing instanceof ResourceContainer) {
            for (com.sbg.bdd.resource.Resource child : ((ResourceContainer) existing).list()) {
                addRecursively(suffix, child, result);
            }
        } else if (existing.getName().endsWith(suffix)) {
            result.add(new GenericResourceCucumberResource(root, existing));
        }
    }
}
