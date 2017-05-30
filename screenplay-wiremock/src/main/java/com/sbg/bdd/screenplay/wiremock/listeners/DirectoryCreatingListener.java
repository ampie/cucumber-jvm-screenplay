//package cucumber.wiremock.listeners;
//
//
//import cucumber.scoping.UserInScope;
//import cucumber.scoping.annotations.ActorListener;
//import cucumber.scoping.annotations.ActorInvolvement;
//
//import java.io.File;
//import java.io.File;
//import java.io.File;
//
//public class DirectoryCreatingListener {
//    @ActorListener(involvement = ActorInvolvement.BEFORE_ENTER_STAGE)
//    public void createOutputDirectoryIfAbsent(UserInScope userInScope) {
//        //TODO do we actually need this?
//        Path outputResourceRoot = userInScope.recall("outputResourceRoot");
//        String resourceRoot = outputResourceRoot.toString();
//        Path personaPath = Paths.get(resourceRoot, userInScope.getId());
//        Path path = Paths.get(personaPath.toString(), userInScope.getScope().getScopePath().split("\\/"));
//        File file = path.toFile();
//        if (!file.exists()) {
//            file.mkdirs();
//        }
//    }
//}
