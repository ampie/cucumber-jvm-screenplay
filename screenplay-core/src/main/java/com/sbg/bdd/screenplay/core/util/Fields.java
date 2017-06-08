package com.sbg.bdd.screenplay.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;


public class Fields {
    private static final Logger LOGGER = LoggerFactory.getLogger(Fields.class);
    private final Class<?> clazz;

    public static Fields of(final Class<?> testClass) {
        return new Fields(testClass);
    }

    private Fields(Class<?> clazz) {
        this.clazz = clazz;
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
                        fieldValues.put(field.getName(), fieldValueFrom(field));
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

        private Object fieldValueFrom(Field field) throws IllegalAccessException {
            return ((field == null) || (object == null) || (field.get(object) == null)) ? null : field.get(object);
        }

    }

}
