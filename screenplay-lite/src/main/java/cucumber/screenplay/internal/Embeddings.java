package cucumber.screenplay.internal;

import cucumber.screenplay.annotations.ProducesAttachment;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


public class Embeddings {
    public static List<Pair<String, byte[]>> producedBy(Object o) {
        List<Pair<String, byte[]>> result = new ArrayList<>();
        for (Field field : FieldUtils.getAllFields(o.getClass())) {
            if (field.isAnnotationPresent(ProducesAttachment.class)) {
                Object dataObject = readValue(o, field);
                if (dataObject != null) {
                    result.add(asEmbedding(field, dataObject));
                }
            }
        }
        return result;
    }

    private static Object readValue(Object o, Field field) {
        try {
            field.setAccessible(true);
            return field.get(o);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ImmutablePair<String, byte[]> asEmbedding(Field field, Object dataObject) {
        ProducesAttachment annotation = field.getAnnotation(ProducesAttachment.class);
        byte[] data = null;
        if (dataObject instanceof byte[]) {
            data = (byte[]) dataObject;
        } else if (dataObject instanceof String) {
            data = ((String) dataObject).getBytes();
        } else if (!Modifier.isAbstract(annotation.serializer().getModifiers())) {
            try {
                data = annotation.serializer().newInstance().toByteArray(dataObject);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        return new ImmutablePair<>(annotation.mimeType(), data);
    }
}
