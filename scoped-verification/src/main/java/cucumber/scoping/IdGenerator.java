package cucumber.scoping;

public class IdGenerator {
    public static String fromName(String name){
        return name.replaceAll("[^\\w]","_");
    }
}
