package cucumber.screenplay;


import java.util.HashMap;
import java.util.Map;

public class SimpleMemory implements Memory {
    private final Map<String, Object> storage=new HashMap<>();

    @Override
    public void remember(String name, Object value) {
        storage.put(name,value);
    }

    @Override
    public void remember(Object value) {
        storage.put(value.getClass().getName(),value);
    }

    @Override
    public void forget(String name) {
        storage.remove(name);
    }

    @Override
    public <T> T recall(String name) {
        return (T) storage.get(name);
    }

    @Override
    public <T> T recall(Class<T> clzz) {
        return recall(clzz.getName());
    }
}
