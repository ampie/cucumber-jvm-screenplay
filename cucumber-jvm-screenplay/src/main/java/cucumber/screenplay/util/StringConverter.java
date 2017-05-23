package cucumber.screenplay.util;

import cucumber.deps.com.thoughtworks.xstream.converters.ConversionException;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.xstream.LocalizedXStreams;

import java.util.Locale;

public class StringConverter {
    private static final ThreadLocal<LocalizedXStreams> localizedXStreams = new ThreadLocal<>();

    public static String toString(Object object) {
        if (object == null) {
            return "null";
        }
        return getConverter(object).toString(object);
    }

    public static SingleValueConverter getConverter(Object object) {
        try {
            return getCurrent().get(Locale.US).getSingleValueConverter(object.getClass());
        } catch (ConversionException e) {
            return null;
        }
    }

    private static LocalizedXStreams getCurrent() {
        if (localizedXStreams.get() == null) {
            localizedXStreams.set(new LocalizedXStreams(Thread.currentThread().getContextClassLoader()));
        }
        return localizedXStreams.get();
    }

}
