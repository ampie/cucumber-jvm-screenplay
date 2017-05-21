package cucumber.screenplay.util;

public class Uninstrumented {
    public static <T> Class<T> versionOf(Class<T> questionClass) {
        return questionClass.getName().contains("EnhancerByCGLIB") ? (Class<T>) questionClass.getSuperclass() : questionClass;
    }
}
