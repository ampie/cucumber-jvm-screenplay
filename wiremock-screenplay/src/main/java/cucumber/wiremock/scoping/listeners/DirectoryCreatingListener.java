//package cucumber.wiremock.listeners;
//
//
//import cucumber.scoping.UserInScope;
//import cucumber.scoping.annotations.SubscribeToUser;
//import cucumber.scoping.annotations.UserInvolvement;
//
//import java.io.File;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//public class DirectoryCreatingListener {
//    @SubscribeToUser(involvement = UserInvolvement.BEFORE_ENTER_STAGE)
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
