package cucumber.screenplay.formatter;

import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.screenplay.annotations.ProducesEmbedding;
import cucumber.screenplay.util.StringConverter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Embeddings {
    public static List<Pair<String, byte[]>> producedBy(Object o) {
        List<Pair<String, byte[]>> result = new ArrayList<>();
        for (Field field : FieldUtils.getAllFields(o.getClass())) {
            if (field.isAnnotationPresent(ProducesEmbedding.class)) {
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
        byte[] data = null;
        if (dataObject instanceof byte[]) {
            data = (byte[]) dataObject;
        } else if (dataObject instanceof String) {
            data = ((String) dataObject).getBytes();
        } else {
            SingleValueConverter converter = StringConverter.getConverter(dataObject);
            if (converter == null) {
                data = dataObject.toString().getBytes();
            } else {
                data = converter.toString(dataObject).getBytes();
            }
        }
        return new ImmutablePair<>(field.getAnnotation(ProducesEmbedding.class).mimeType(), data);
    }
}
