package cucumber.screenplay.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;


public class Fields {
    public enum FieldValue {
        UNDEFINED
    }


    private static final Logger LOGGER = LoggerFactory.getLogger(Fields.class);
    private final Class<?> clazz;

    public static Fields of(final Class<?> testClass) {
        return new Fields(testClass);
    }

    private Fields(Class<?> clazz) {
        this.clazz = clazz;
    }
    public Optional<Field> withName(String pages) {
        for(Field field : allFields()) {
            if (field.getName().equals(pages)){
                return Optional.of(field);
            }
        }
        return Optional.absent();
    }
    public Set<Field> allFields() {
        Set<Field> fields = new HashSet<Field>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        fields.addAll(Arrays.asList(clazz.getFields()));
        if (clazz != Object.class) {
            fields.addAll(Fields.of(clazz.getSuperclass()).allFields());
        }
        return fields;
    }


    public static FieldValueBuilder of(Object object) {
        return new FieldValueBuilder(object);
    }

    public static class FieldValueBuilder {
        private final Object object;

        public FieldValueBuilder(Object object) {
            this.object = object;
        }

        public Map<String, Object> asMap() {
            Map<String, Object> fieldValues = new HashMap<>();
            for (Field field : Fields.of(object.getClass()).allFields()) {
                try {
                    field.setAccessible(true);
                    if (isValid(field)) {
                        fieldValues.put(field.getName(), fieldValueFrom(field).or(FieldValue.UNDEFINED));
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.warn("Failed to inject the field " + field.getName(), e);
                }
            }
            fieldValues.put("self", object);
            fieldValues.put("this", object);
            return Collections.unmodifiableMap(fieldValues);
        }

        private boolean isValid(Field field) {
            return ((field != null) && (!field.getName().contains("CGLIB")));
        }

        private FieldValueProvider fieldValueFrom(Field field) {
            return new FieldValueProvider(field, object);
        }

        private static class FieldValueProvider {
            Field field;
            Object object;

            public FieldValueProvider(Field field, Object object) {
                this.field = field;
                this.object = object;
            }

            public Object or(FieldValue undefinedValue) throws IllegalAccessException {
                return ((field == null) || (object == null) || (field.get(object) == null)) ? undefinedValue : field.get(object);
            }
        }
    }

}
