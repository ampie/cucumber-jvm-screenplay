package cucumber.scoping.wiremock;


import cucumber.screenplay.Scene;

import java.io.File;

public class RelativeResourceDir {
    public static String of(Scene scope) {
        //TODO should we call it scene.path?
        String path = scope.getIdentifier();
        if (path.lastIndexOf("/") <= 0) {
            return "";
        } else {

            return path.substring(path.indexOf("/") + 1).replace('/', File.separatorChar);
        }
    }
}
