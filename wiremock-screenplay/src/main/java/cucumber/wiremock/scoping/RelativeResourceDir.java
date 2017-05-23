package cucumber.wiremock.scoping;


import cucumber.scoping.GlobalScope;
import cucumber.scoping.UserTrackingScope;

import java.io.File;

public class RelativeResourceDir {
    public static String of(UserTrackingScope scope) {
        if (scope instanceof GlobalScope) {
            return "";
        } else {
            String scopePath = scope.getScopePath();
            return scopePath.substring(scopePath.indexOf("/") + 1).replace('/', File.separatorChar);
        }
    }
}
