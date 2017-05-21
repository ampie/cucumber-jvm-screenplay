package cucumber.screenplay;


public interface Memory {
    void remember(String name, Object value);

    void remember(Object value);

    void forget(String name);

    <T> T recall(String name);

    <T> T recall(Class<T> clzz);
}
