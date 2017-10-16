package examplepackage;

import com.sbg.bdd.cucumber.wiremock.annotations.ResourceRoots;
import com.sbg.bdd.cucumber.wiremock.annotations.ScreenplayUrls;
import com.sbg.bdd.resource.ResourceContainer;
import com.sbg.bdd.resource.file.DirectoryResourceRoot;

import java.io.File;
import java.net.URL;

public class ExampleResourceRoots implements ResourceRoots{
    @Override
    public ResourceContainer getFeatureFileRoot(ScreenplayUrls urls) {
        return (ResourceContainer) getInputRoot(urls).getChild("features");
    }

    @Override
    public ResourceContainer getPersonaRoot(ScreenplayUrls urls) {
        return (ResourceContainer) getInputRoot(urls).getChild("personas");
    }

    @Override
    public ResourceContainer getInputRoot(ScreenplayUrls urls) {
        File markerFile = getResourceRoot();
        return new DirectoryResourceRoot("input", markerFile);
    }

    private File getResourceRoot() {
        URL marker = ExampleResourceRoots.class.getClassLoader().getResource("cucumber-jvm-screenplay-junit-marker.txt");
        return new File(marker.getFile()).getParentFile();
    }

    @Override
    public ResourceContainer getOutputRoot(ScreenplayUrls urls) {
        return new DirectoryResourceRoot("outputRoot", new File(getResourceRoot().getParentFile(), "output"));
    }

    @Override
    public ResourceContainer getJournalRoot(ScreenplayUrls urls) {
        return new DirectoryResourceRoot("journalRoot", new File(getResourceRoot().getParentFile(), "journal"));
    }
}
