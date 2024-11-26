package me.t65.reportgenapi.config.converters;

import org.springframework.format.Parser;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

public class CaseInsensitiveEnumParser<T extends Enum> implements Parser<T> {

    private final Class<? extends Enum> enumType;
    private final String[] valueNames;

    public CaseInsensitiveEnumParser(Class<?> type) {
        this.enumType = type.asSubclass(Enum.class);
        Object[] values = type.getEnumConstants();

        if (values == null) {
            throw new IllegalArgumentException("No values for " + type);
        }

        this.valueNames =
                Arrays.stream(values).map(value -> ((Enum<?>) value).name()).toArray(String[]::new);
    }

    @Override
    public T parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }

        for (String name : valueNames) {
            if (name.equalsIgnoreCase(text)) {
                return (T) Enum.valueOf(enumType, name);
            }
        }
        throw new ParseException(
                "No enum constant " + enumType.getCanonicalName() + " found for value " + text, 0);
    }
}
