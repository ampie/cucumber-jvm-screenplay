package cucumber.screenplay.internal;

import java.util.HashMap;
import java.util.Map;

public class SimpleInstanceGetter implements InstanceGetter {
    Map<Class<?>, Object> instances = new HashMap<>();

    @Override
    public <T> T getInstance(Class<T> type) {
        try {
            T result = (T) instances.get(type);
            if (result == null) {
                result = type.newInstance();
                instances.put(type, result);
            }
            return result;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
